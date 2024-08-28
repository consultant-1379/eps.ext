/*
 * ------------------------------------------------------------------------------
 * *****************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 * *****************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.component.aia.services.exteps.eh.parser;

import static com.ericsson.component.aia.services.exteps.eh.parser.IpAddressUtil.getReadableIPAddress;

import java.io.IOException;
import java.util.*;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.ericsson.component.aia.itpf.common.event.handler.AbstractApplicationEventHandler;
import com.ericsson.component.aia.mediation.parsers.exception.LoadingFailedException;
import com.ericsson.component.aia.mediation.parsers.exception.ParsingFailedException;
import com.ericsson.component.aia.mediation.parsers.parser.*;
import com.ericsson.component.aia.mediation.parsers.receiver.DecodedEventReceiver;
import com.ericsson.component.aia.mediation.parsers.streamrecord.StreamedRecord;
import com.ericsson.component.aia.model.base.config.bean.DecodedEventType;
import com.ericsson.component.aia.model.base.config.bean.SchemaEnum;
import com.ericsson.component.aia.services.exteps.io.common.statistics.EpsExtStatisticsHelper;
import com.ericsson.component.aia.model.base.config.bean.SchemaProviderType;

/**
 * Created by anauser on 30/05/16.
 */
public class PmRecordStreamParser extends AbstractApplicationEventHandler {


    public static final String SUBNETWORK = "subnetwork";
    public static final String UTC_OFFSET = "utcoffset";
    public static final String SCHEMA_TYPE = "schematype";
    public static final String DECODER_TYPE = "decodedEventType";
    protected static final String EPS_EXT = "epsExt";

    protected Map<Integer, Parser> parserMap = new HashMap<>();
    protected Set<Integer> invalidSrcSet = new HashSet<>();
    protected DecodedEventType outputType;
    protected SchemaEnum schemaType;

    protected String subNetworkName;
    protected String timezone;
    protected Meter pmRecordStreamEventMeter;
    protected Meter eventProcessedMeter;
    protected Meter connectionMeter;
    protected Meter disconnectMeter;
    protected Meter missedConnectionMeter;
    protected Gauge<Long> queueSizeGauge;

    protected EpsExtStatisticsHelper epsExtStatisticsHelper;

    PmRecordStreamParser(final Map<Integer, Parser> parserMap, final Set<Integer> invalidSrcSet) {
        this.parserMap = parserMap;
        this.invalidSrcSet = invalidSrcSet;
    }

    /**
     * Default Constructor
     */
    public PmRecordStreamParser() {
    }

    @Override
    public boolean understandsURI(final String uri) {
        // Correct uri will be "parser:/celltrace" or "parser:/ctum" etc.
        if (!uri.startsWith("parser:/")) {
            return false;
        }
        final String parserType = uri.substring(uri.lastIndexOf("/") + 1, uri.length());
        try {
            schemaType = SchemaEnum.fromValue(parserType);
            return true;
        } catch (final IllegalArgumentException iaex) {
            return false;
        }
    }

    /**
     * Invoked only once during initialization but before any event processing.
     */
    @Override
    protected void doInit() {
        log.trace("Retrieving Parser parameters ...");
        subNetworkName = getStringProperty(SUBNETWORK);
        timezone = getStringProperty(UTC_OFFSET);
        epsExtStatisticsHelper = new PmRecordStreamStatistics(this.getClass().getSimpleName());
        epsExtStatisticsHelper.initialiseStatistics(getEventHandlerContext());
        setSchemaType();
        setDecoderType();
    }

    /**
     * Set the decoded output type, will revert to avro generic record if the type is malformed
     */
    protected void setDecoderType() {
        final String decoderType = getStringProperty(DECODER_TYPE);
        try {
            outputType = DecodedEventType.fromValue(decoderType);
        } catch (final IllegalArgumentException iaEx) {
            log.error("Cannot determine decoder type from property {}={}, using avro Generic Record", DECODER_TYPE, decoderType);
            outputType = DecodedEventType.GENERIC_RECORD;
        }
    }

    /**
     * Set the Schema Type for the Parsers.
     */
    private void setSchemaType() {
        String schema = "";
        try {
            schema = getStringProperty(SCHEMA_TYPE);
            schemaType = SchemaEnum.fromValue(schema);
        } catch (final IllegalArgumentException iae) {
            log.error("Schema Type {} not recognised. Details {}", schema, iae.getMessage());
            throw new IllegalStateException("Schema Type " + schema + " not recognised.", iae);
        }
    }

    /**
     * Creates a new Parser instance.
     *
     * @return
     */
    protected Parser getParser() {
        StreamParser streamParser = null;
        try {
            streamParser = new StreamParser(schemaType, SchemaProviderType.FILE_BASED, getEventBeanListener(), subNetworkName, timezone,
                    outputType);
        } catch (final ParsingFailedException e) {
            log.error("Failed to create StreamParser object: " + e);
        }
        return streamParser;
    }

    /**
     * Invoked by flow engine on receipt of receipt of input event(s)
     *
     * @param object
     */
    @Override
    public void inputEvents(final Object object) {
        if (object instanceof StreamedRecord) {
            final StreamedRecord record = (StreamedRecord) object;
            final StreamedRecord.Actions action = record.getAction();
            try {
                switch (action) {
                    case CONNECT: {
                        handleConnect(record);
                        break;
                    }
                    case EVENT: {
                        handleEvent(record);
                        break;
                    }
                    case DISCONNECT: {
                        handleDisconnect(record);
                        break;
                    }
                    default: {
                        log.warn("Unexpected record: {}", action);
                    }
                }
            } catch (LoadingFailedException | IOException exception) {
                log.error("Exception Parsing StreamRecord[{}] {} :: {} :: {}", action, record.getSourceId(),
                        getReadableIPAddress(record.getRemoteIP()), exception.getMessage());
                if (action.equals(StreamedRecord.Actions.CONNECT)) {
                    log.error(
                            "Events with SourceId[{}] and IP Address[{}] will not be processed until a valid fileFormatVersion and "
                            + "fileInformationVersion is received.",
                            record.getSourceId(), getReadableIPAddress(record.getRemoteIP()));
                    invalidSrcSet.add(record.getSourceId());
                }
            }
        }
    }

    /**
     * Adds a parser to the hashmap. A connection represents a new node.
     *
     * @param myRecordParam
     * @throws IOException
     * @throws LoadingFailedException
     */
    private void handleConnect(final StreamedRecord myRecordParam) throws IOException, LoadingFailedException {
        final int sourceId = myRecordParam.getSourceId();
        final Parser parser = getParser();

        if (parser != null) {
            ((StreamParser) parser).execute(myRecordParam);
            parserMap.put(sourceId, parser);
        } else {
            log.error("Parser was initialised to null");
        }

        if (invalidSrcSet.contains(sourceId)) {
            log.info(
                    "Events with SourceId[{}] and IP Address[{}] will now be processed, a valid fileFormatVersion and fileInformationVersion was "
                    + "received.",
                    myRecordParam.getSourceId(), getReadableIPAddress(myRecordParam.getRemoteIP()));
            invalidSrcSet.remove(sourceId);
        }
        if (epsExtStatisticsHelper.isStatisticsOn()) {
            epsExtStatisticsHelper.markMeter(connectionMeter);
        }
    }

    /**
     * Removes the Parser from the Hashmap. No further events from this node.
     *
     * @param myRecordParam
     */
    private void handleDisconnect(final StreamedRecord myRecordParam) {
        final int sourceId = myRecordParam.getSourceId();
        parserMap.remove(sourceId);
        if (epsExtStatisticsHelper.isStatisticsOn()) {
            epsExtStatisticsHelper.markMeter(disconnectMeter);
        }
    }

    /**
     * Retrieves a parser from the hashmap based on sourceId. Use parser to decode event.
     *
     * @param myRecordParam
     * @throws IOException
     * @throws LoadingFailedException
     */
    private void handleEvent(final StreamedRecord myRecordParam) throws IOException, LoadingFailedException {
        final int sourceId = myRecordParam.getSourceId();
        if (epsExtStatisticsHelper.isStatisticsOn()) {
            epsExtStatisticsHelper.markMeter(pmRecordStreamEventMeter);
        }
        final Parser parser = parserMap.get(sourceId);
        if (parser != null) {
            ((StreamParser) parser).execute(myRecordParam);
        } else {
            if (epsExtStatisticsHelper.isStatisticsOn()) {
                epsExtStatisticsHelper.markMeter(missedConnectionMeter);
            }
        }
    }

    /**
     * Sends specified event to all subscribers. Increments counter if statistics enabled.
     *
     * @param inputEvent the event to send to subscribers
     */
    @Override
    public void sendEvent(final Object inputEvent) {
        sendToAllSubscribers(inputEvent);
        if (epsExtStatisticsHelper.isStatisticsOn()) {
            epsExtStatisticsHelper.markMeter(eventProcessedMeter);
        }
    }

    /**
     * Creates a new EventBeanListener.
     *
     * @return
     */
    protected DecodedEventReceiver getEventBeanListener() {
        return new EventBeanListener(this);
    }

    @Override
    public void destroyAll() {

    }

    private class PmRecordStreamStatistics extends EpsExtStatisticsHelper {

        PmRecordStreamStatistics(String clazzName) {
            super(clazzName);
        }

        @Override
        public void registerStatisticsMetrics() {
            final String regName = EPS_EXT + "_" + PmRecordStreamParser.class.getSimpleName() + "_";
            createEventMeter(regName);
            registerParserMapSizeGauge(regName);
        }

        protected void createEventMeter(final String regName) {
            pmRecordStreamEventMeter = epsStatisticsRegister.createMeter(regName + "eventsReceived");
            eventProcessedMeter = epsStatisticsRegister.createMeter(regName + "eventsProcessed");
            connectionMeter = epsStatisticsRegister.createMeter(regName + "connectionsReceived");
            disconnectMeter = epsStatisticsRegister.createMeter(regName + "disconnectsReceived");
            missedConnectionMeter = epsStatisticsRegister.createMeter(regName + "missedConnectionProcessed");
        }
        private void registerParserMapSizeGauge(final String regName) {
            queueSizeGauge = new Gauge<Long>() {

                @Override
                public Long getValue() {
                    if (parserMap != null) {
                        return (long) parserMap.size();
                    }
                    return -1L;
                }
            };
            epsStatisticsRegister.registerGuage(regName + "ParserMapSize", queueSizeGauge);
        }
    }
}
