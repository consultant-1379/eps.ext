/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.exteps.io.adapter.util;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler;
import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;
import com.ericsson.component.aia.mediation.netty.protocol.parser.*;
import com.ericsson.component.aia.mediation.parsers.streamrecord.StreamedRecord;
import com.ericsson.component.aia.services.exteps.io.common.statistics.EpsExtStatisticsHelper;
import com.google.common.base.Optional;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

/**
 * The Class RawBytesConverter. Processed batched undecoded byte arrays consumed from Kafka and transforms them into a list of StreamedRecords.
 */
public class RawBytesConverter extends AbstractEventHandler implements EventInputHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RawBytesConverter.class);
    private static final int STREAM_OUT_PROTOCOL_HEADER_LENGTH = 4;
    private static DefaultStreamOutProtocolParser parser;
    private static Map<Integer, byte[]> ipAddressCache;

    private EpsExtStatisticsHelper epsExtStatisticsHelper;

    @Override
    protected void doInit() {
        parser = new DefaultStreamOutProtocolParser();
        ipAddressCache = new HashMap<>();
        epsExtStatisticsHelper = new EpsExtStatisticsHelper(this.getClass().getSimpleName());
        epsExtStatisticsHelper.initialiseStatistics(getEventHandlerContext());
    }

    @Override
    public void onEvent(final Object inputEvent) {
        checkInputEventType(inputEvent);
        final List<StreamedRecord> streamedRecords = splitBytesIntoStreamedRecords((byte[]) inputEvent);
        for (final StreamedRecord streamedRecord : streamedRecords) {
            sendToAllSubscribers(streamedRecord);
        }
        if (epsExtStatisticsHelper.isStatisticsOn()) {
            epsExtStatisticsHelper.mark();
        }
    }

    /**
     * Checks input event is correct type (byte[])
     *
     * @param inputEvent
     *            byte array
     */
    protected void checkInputEventType(final Object inputEvent) {
        if (inputEvent == null) {
            throw new IllegalArgumentException("Input event must not be null");
        }
        final boolean correctEventType = inputEvent instanceof byte[];
        if (!correctEventType) {
            throw new IllegalArgumentException("Input event must be a type of byte[]");
        }
    }

    /**
     * Splits a 'batched byte[]' (a byte[] which contains multiple events in byte[] format) into a collection of StreamedRecords.
     *
     * @param bytes
     *            the batched bytes
     * @return the collection of StreamedRecords
     */
    public List<StreamedRecord> splitBytesIntoStreamedRecords(final byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException("Bytes must not be null");
        }
        final List<StreamedRecord> streamedRecords = new ArrayList<>();
        final ByteBuf batchedEvents = ByteBufAllocator.DEFAULT.buffer(bytes.length).writeBytes(bytes);
        while (batchedEvents.isReadable()) {
            final int length = batchedEvents.getUnsignedShort(batchedEvents.readerIndex() + STREAM_OUT_PROTOCOL_HEADER_LENGTH);
            final byte[] message = new byte[length + STREAM_OUT_PROTOCOL_HEADER_LENGTH];
            batchedEvents.readBytes(message);
            final ByteBuf bufferedEvent = ByteBufAllocator.DEFAULT.buffer(message.length).writeBytes(message);
            final AbstractMuxEvent multiplexEvent = parser.parse(bufferedEvent);
            final Optional<StreamedRecord> record = createOptionalStreamedRecord(multiplexEvent);
            if (record.isPresent()) {
                streamedRecords.add(record.get());
            }
            bufferedEvent.release();
        }
        batchedEvents.release();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Number of records send to next handler: {}.", streamedRecords.size());
        }
        return streamedRecords;
    }

    /**
     * Creates the optional containing a streamed record. If event is of type 'init' or 'dropped' or just unknown, an empty optional is returned.
     *
     * @param event
     *            the event
     * @return an empty optional
     */
    private Optional<StreamedRecord> createOptionalStreamedRecord(final AbstractMuxEvent event) {
        switch (event.eventType()) {
            case CONNECTION:
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Received a connection event with sourceId: {}.", event.sourceId());
                }
                return Optional.of(createStreamedRecord((MuxConnection) event));
            case DISCONNECTION:
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Received a disconnection event with sourceId: {}.", event.sourceId());
                    LOGGER.debug("Reason for disconnect: {}", ((MuxDisconnection) event).reason().name());
                }
                return Optional.of(createStreamedRecord((MuxDisconnection) event));
            case PAYLOAD:
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Received a payload event with sourceId: {}.", event.sourceId());
                }
                return Optional.of(createStreamedRecord((MuxEvent) event));
            case DROPPED:
                LOGGER.warn("Received a dropped event with sourceId: {}.", event.sourceId());
                LOGGER.warn("Reason for dropped event: {}.", ((MuxDropped) event).reason().name());
                return Optional.absent();
            case INIT:
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Received a init event with sourceId: {}.", event.sourceId());
                    LOGGER.debug("Protocol Version: {}.", ((MuxInit) event).protocolVersion());
                }
                return Optional.absent();
            default:
                LOGGER.error("Received an unknown event with sourceId: {}.", event.sourceId());
                return Optional.absent();
        }
    }

    private StreamedRecord createStreamedRecord(final MuxConnection connectionEvent) {
        final int sourceId = connectionEvent.sourceId();
        final byte[] ipAddress = connectionEvent.ip();
        final byte[] payload = toByteArray(connectionEvent.payload());
        ipAddressCache.put(sourceId, ipAddress);

        final StreamedRecord streamedRecord = new StreamedRecord(sourceId);
        streamedRecord.setAction(StreamedRecord.Actions.CONNECT);
        streamedRecord.setRemoteIP(ipAddress);
        streamedRecord.setData(payload);
        return streamedRecord;
    }

    private StreamedRecord createStreamedRecord(final MuxDisconnection disconnectionEvent) {
        final int sourceId = disconnectionEvent.sourceId();
        final byte[] ipAddress = ipAddressCache.get(sourceId);
        final short disconnectReason = disconnectionEvent.reason().reason();
        final StreamedRecord streamedRecord = new StreamedRecord(sourceId);
        streamedRecord.setAction(StreamedRecord.Actions.DISCONNECT);
        streamedRecord.setRemoteIP(ipAddress);
        streamedRecord.setDisconnectReason(disconnectReason);
        return streamedRecord;
    }

    private StreamedRecord createStreamedRecord(final MuxEvent payloadEvent) {
        final int sourceId = payloadEvent.sourceId();
        final byte[] ipAddress = ipAddressCache.get(sourceId);
        final byte[] payload = toByteArray(payloadEvent.payload());

        final StreamedRecord streamedRecord = new StreamedRecord(sourceId);
        streamedRecord.setAction(StreamedRecord.Actions.EVENT);
        streamedRecord.setRemoteIP(ipAddress);
        streamedRecord.setData(payload);
        return streamedRecord;
    }

    /**
     * Converts from {@link ByteBuf} to {@code byte[]}.
     *
     * @param payloadBuffer
     *            the payload to be converted
     * @return byte[] representation
     */
    byte[] toByteArray(final ByteBuf payloadBuffer) {
        final byte[] payload = new byte[payloadBuffer.readableBytes()];
        payloadBuffer.getBytes(0, payload);
        return payload;
    }
}
