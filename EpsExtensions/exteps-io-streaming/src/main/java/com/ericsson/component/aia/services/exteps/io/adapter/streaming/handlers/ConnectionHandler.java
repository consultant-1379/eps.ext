/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.exteps.io.adapter.streaming.handlers;

import io.netty.buffer.ByteBuf;

import com.ericsson.component.aia.mediation.netty.protocol.parser.AbstractMuxEvent;
import com.ericsson.component.aia.mediation.netty.protocol.parser.MuxConnection;
import com.ericsson.component.aia.mediation.parsers.streamrecord.StreamedRecord;

/**
 * This class is responsible for handling the stream connection messages.
 *
 * @since 1.0.7
 *
 **/
public class ConnectionHandler extends AbstractStreamHandler {

    @Override
    public void handle(final AbstractMuxEvent event) {
        final int sourceId = event.sourceId();
        checkValidSoruceId(sourceId);
        handleConnect((MuxConnection) event);
        incrementConnects();
    }

    protected void checkValidSoruceId(final int sourceId) {
        if (VALID_SRC_SET.contains(sourceId)) {
            LOGGER.warn("Got another connection message for {} ", sourceId);
        } else {
            LOGGER.trace("Received connection message for {}", sourceId);
            VALID_SRC_SET.add(sourceId);
            if (INVALID_SRC_SET.contains(sourceId)) {
                INVALID_SRC_SET.remove(sourceId);
                LOGGER.trace("Connection message for {} removed from invalid source set", sourceId);
            }
        }
    }

    /**
     * This method handles connection of a node on an event stream
     *
     * @param event
     */
    protected void handleConnect(final MuxConnection event) {
        final int sourceId = event.sourceId();
        final StreamedRecord streamedRecord = new StreamedRecord(sourceId);
        streamedRecord.setAction(StreamedRecord.Actions.CONNECT);
        final byte[] ipAddress = event.ip();
        streamedRecord.setRemoteIP(ipAddress);

        final ByteBuf buff = event.payload();
        final byte[] payload = new byte[buff.capacity()];
        buff.getBytes(0, payload);
        streamedRecord.setData(payload);
        SOURCEID_IPADDRESS.put(Integer.valueOf(sourceId), ipAddress);
        offer(streamedRecord);
        LOGGER.debug("Connection message for IP ADDRESS :: {}", ipAddress);
    }
}
