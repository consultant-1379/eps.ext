/*
 * ------------------------------------------------------------------------------
 * *****************************************************************************
 * COPYRIGHT Ericsson 2018
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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.ericsson.component.aia.itpf.common.event.handler.AbstractApplicationEventHandler;
import com.ericsson.component.aia.mediation.event.exception.FileEventFilterException;
import com.ericsson.component.aia.mediation.event.service.FilterEventReader;
import com.ericsson.component.aia.mediation.event.util.EventTypeFilterRetrieverUtil;
import com.ericsson.component.aia.mediation.parsers.exception.ParsingFailedException;
import com.ericsson.component.aia.mediation.parsers.parser.FileParser;
import com.ericsson.component.aia.mediation.parsers.receiver.DecodedEventReceiver;
import com.ericsson.component.aia.model.base.config.bean.DecodedEventType;
import com.ericsson.component.aia.model.base.config.bean.SchemaEnum;
import com.ericsson.component.aia.model.base.config.bean.SchemaProviderType;
import com.ericsson.component.aia.model.base.util.metrics.EventMetrics;
import com.ericsson.component.aia.services.eps.EpsEngineConstants;
import com.ericsson.component.aia.services.eps.statistics.EpsStatisticsRegister;
import com.ericsson.component.aia.services.exteps.eh.parser.exception.EventFilterServiceLocatorException;
import com.ericsson.component.aia.services.exteps.eh.parser.util.EventFilterServiceLocatorHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The <code>PMFileParser</code> is PM record parser which is able to take input in various format.
 * <ol>
 * <li>JsonString "{'path' : '/tmp/enm/ebrs/abc-pqr.bin'}" representing com.fasterxml.jackson.databind.JsonNode object</li>
 * <li>JsonString "{'path' : '/tmp/enm/ebrs/abc-pqr.bin'}" representing java string object</li>
 * <li>java.io.File object representing file location</li>
 * <li>Collection<java.io.File> object representing collection of file's</li>
 * <ol>
 * <br>
 * It retrieves the file information and parse the file with {@link com.ericsson.component.aia.mediation.parsers.parser.FileParser}
 */
public class PMFileParser extends AbstractApplicationEventHandler {

    public static final String SCHEMA_TYPE = "schematype";
    public static final String DECODER_TYPE = "decodedEventType";
    protected static final String EPS_EXT = "epsExt";
    private static final String PATH = "path";
    private static final String EVENT_FILTER_PATH = "eventFilterPath";

    protected DecodedEventType outputType;
    protected FileParser parser;
    protected SchemaEnum schemaType;

    /**
     * The Statistics Register assist to created relevant type of stats.
     */
    protected EpsStatisticsRegister statisticsRegister;
    /**
     * The Records meter will record all the events including header and footer.
     */
    protected Meter recordsMeter;
    /**
     * The Event Processed meter records the number of events process by the parser.
     */
    protected Meter eventsProcessedMeter;
    /**
     * The erroneous file counter records the invalid files.
     */
    protected Counter erroneousFilesCounter;
    /**
     * The invalid events counter records the invalid events found during parsing.
     */
    protected Counter invalidEventsCounter;
    /**
     * The ignored events counter are those events which are ignored during parsing.
     */
    protected Counter ignoredEventsCounter;
    /**
     * file processing time meter is amount of time taken by parser to parse complete file.
     */
    protected Meter fileProcessingTimeMeter;
    /**
     * Files count counter count the number of files.
     */
    protected Counter filesCountCounter;

    @Override
    public boolean understandsURI(final String uri) {
        // Correct uri will be "fileparser:/celltrace"
        if (!uri.startsWith("fileparser:/")) {
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
        if (schemaType == null) {
            setSchemaType();
        }
        setDecoderType();
        initialiseStatistics();
        getParser();
    }

    /**
     * Initialize statistics.
     */
    protected void initialiseStatistics() {
        statisticsRegister = (EpsStatisticsRegister) getEventHandlerContext()
                .getContextualData(EpsEngineConstants.STATISTICS_REGISTER_CONTEXTUAL_DATA_NAME);
        if (statisticsRegister == null) {
            log.error("statisticsRegister should not be null");
        } else {
            if (statisticsRegister.isStatisticsOn()) {
                recordsMeter = statisticsRegister.createMeter(PMFileParser.class.getSimpleName() + "@" + "records");
                eventsProcessedMeter = statisticsRegister.createMeter(PMFileParser.class.getSimpleName() + "@" + "eventsProcessed");
                erroneousFilesCounter = statisticsRegister.createCounter(PMFileParser.class.getSimpleName() + "@" + "erroneousFiles");
                invalidEventsCounter = statisticsRegister.createCounter(PMFileParser.class.getSimpleName() + "@" + "invalidEvents");
                ignoredEventsCounter = statisticsRegister.createCounter(PMFileParser.class.getSimpleName() + "@" + "ignoredEvents");
                filesCountCounter = statisticsRegister.createCounter(PMFileParser.class.getSimpleName() + "@" + "filecounts");
                fileProcessingTimeMeter = statisticsRegister.createMeter(PMFileParser.class.getSimpleName() + "@" + "fileprocessingtime");
            }
        }
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
            schemaType = com.ericsson.component.aia.model.base.config.bean.SchemaEnum.fromValue(schema);
        } catch (final IllegalArgumentException iae) {
            log.error("Schema Type {} not recognised. Details {}", schema, iae.getMessage());
            throw new IllegalStateException("Schema Type " + schema + " not recognised.", iae);
        }
    }

    /**
     * Creates a new Parser instance.
     *
     * @return parser instance.
     */
    protected void getParser() {

        Set<Integer> events = Collections.emptySet();

        try {
            //if filter file exists in flow definition, retrieve events from filter file
            events = retrieveSupportedEvents();

            parser = new FileParser(schemaType, SchemaProviderType.FILE_BASED_ON_DEMAND, getEventBeanListener(), outputType);

            //if events exists, call apply them in Parsers supportedEvents for filtering events
            if (!(events.isEmpty())) {
                log.info("Supported events exists, hence calling Parser's setSupportedEvents for filtering events :: selected events are {}",
                        events.toString());
                parser.setSupportedEvents(events);
            } else {
                log.info("Supported events doesn't exists, hence processes all events");
            }

        } catch (final ParsingFailedException e) {
            log.error("Failed to create FileParser object: ", e);
        } catch (final EventFilterServiceLocatorException e) {
            throw new EventFilterServiceLocatorException("Failed to retrieve service" + e);
        }
    }

    /**
     * Invoked by flow engine on receipt of input event(s) It will handle single file or collection of file as input
     */
    @Override
    public void inputEvents(final Object processInput) {

        if (isFilePathEvent(processInput)) {

            final Collection<File> fileCollection = FileInformationRetrieverUtil.getListOfFiles(processInput);

            log.debug("Retrieved fileCollection size is {}", fileCollection.size());

            if (!fileCollection.isEmpty()) {
                processInputFiles(fileCollection);
            }

        } else {
            log.debug("check whether event types available from input {}", processInput);
            //retrieves event types and sets events if available in parser
            retrieveEventTypesAndUpdateInParser((String) processInput);
        }

    }

    private void retrieveEventTypesAndUpdateInParser(final String input) {

        final Set<Integer> eventTypeSet = EventTypeFilterRetrieverUtil.parseJSONString(input);

        if (!eventTypeSet.isEmpty()) {
            log.info("event types are {}, hence setting eventType(s) in parser", eventTypeSet.toString());
            parser.setSupportedEvents(eventTypeSet);
        } else {
            log.info("events types are unavailble, hence no changes for Parser supportted events");
        }
    }

    /**
     * Process input files. <br>
     * This method has package level access in order to verify the correctness.</br>
     *
     * @param input
     *            collection of input files
     * @return EventMetrics if provided collection is not empty else null.
     */
    EventMetrics processInputFiles(final Collection<File> input) {
        try {
            if (input != null && !input.isEmpty()) {
                final EventMetrics parsingStats = parser.execute(input);
                if (statisticsRegister.isStatisticsOn()) {
                    updateMetrics(parsingStats);
                }
                return parsingStats;
            }
        } catch (final ParsingFailedException e) {
            log.error("Error while parsing file ", e);
        }
        return null;
    }

    /**
     * Sends specified event to all subscribers. Increments counter if statistics enabled.
     *
     * @param inputEvent
     *            the event to send to subscribers
     */
    @Override
    public void sendEvent(final Object inputEvent) {
        sendToAllSubscribers(inputEvent);
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

    /**
     * Update statics.
     *
     * @param eventMetrics
     *            instance of EventMetrics which holds the processing information.
     */
    private void updateMetrics(final EventMetrics eventMetrics) {
        recordsMeter.mark(eventMetrics.getRecords());
        eventsProcessedMeter.mark(eventMetrics.getEventsProcessed());
        erroneousFilesCounter.inc(eventMetrics.getErroneousFiles());
        invalidEventsCounter.inc(eventMetrics.getInvalidEvents());
        ignoredEventsCounter.inc(eventMetrics.getIgnoredEvents());
        filesCountCounter.inc(eventMetrics.getFiles());
        fileProcessingTimeMeter.mark(Math.abs(eventMetrics.getProcessingTimeInMilliSec()));
    }

    /**
     * retrieveFileEvents method retrieves filepath from flow def and calls Parser's FilterEventReader's retrieveEvents service for fetching events
     *
     * @return events in Set<Integer>
     * @throws EventFilterServiceLocatorException
     */
    protected Set<Integer> retrieveSupportedEvents() throws EventFilterServiceLocatorException {

        Set<Integer> eventTypes = Collections.emptySet();

        try {
            //get event filter path from flow definition
            final String eventFilterPath = getStringProperty(EVENT_FILTER_PATH);
            log.debug("Event filter path is {}", eventFilterPath);

            //if eventFilterPath is not empty
            if (StringUtils.isNotEmpty(eventFilterPath)) {

                //calls EventFilterServiceLocatorUtil's validateEventReaderSPI
                final FilterEventReader fileEventReader = EventFilterServiceLocatorHelper.validateEventReaderSPI(eventFilterPath);
                log.info("valid eventReader {}", fileEventReader);

                //checks for fileEventReader not null
                if (fileEventReader != null) {

                    //calls fileEventReader's retrieveEvents method
                    eventTypes = fileEventReader.retrieveEventTypes(eventFilterPath);
                    log.info("events are {}", eventTypes.toString());
                } else {
                    final String errorMessage = String.format("fileEventReader SPI is unavailable, for given filterPath %s", eventFilterPath);
                    log.info(errorMessage);
                    throw new EventFilterServiceLocatorException(errorMessage);
                }
            } else {
                log.info("Event filter path is empty, hence all events will be processed");
            }

        } catch (final IllegalStateException | FileEventFilterException ise) {
            log.warn("error retrieving event filter file, all events will be processed");
        }

        return eventTypes;
    }

    /**
     * isFilePathEvent method extracts the information based on input type and returns true or false. This method verifies if path field exists in
     * String/Json input or checks for input type(s) either File or Collection object. It will handle input types
     * <ol>
     * <li>JsonString "{'path' : '/tmp/enm/ebrs/abc-pqr.bin'}" representing com.fasterxml.jackson.databind.JsonNode object</li>
     * <li>JsonString "{'path' : '/tmp/enm/ebrs/abc-pqr.bin'}" representing java string object</li>
     * <li>java.io.File object representing file location</li>
     * <li>Collection<java.io.File> object representing collection of file's</li>
     * <ol>
     *
     * @return boolean
     */
    public boolean isFilePathEvent(final Object parseInput) {

        if (parseInput instanceof String) {
            // parsing the input as json object
            final ObjectMapper objectMapper = new ObjectMapper();

            try {

                final JsonNode jsonNode = objectMapper.readTree((String) parseInput);

                if (jsonNode != null && !jsonNode.path(PATH).isMissingNode()) {
                    return true;
                }

            } catch (final IOException e) {
                log.error(e.getMessage(), e);
                log.error("Unable to parse the value {} as JSON", parseInput);
            }
        } else if (parseInput instanceof JsonNode) {

            if (!((JsonNode) parseInput).path(PATH).isMissingNode()) {
                return true;
            }

        } else if (parseInput instanceof File || parseInput instanceof Collection) {
            return true;
        }

        log.warn("Unknown input type received  {}", parseInput);
        return false;
    }

}