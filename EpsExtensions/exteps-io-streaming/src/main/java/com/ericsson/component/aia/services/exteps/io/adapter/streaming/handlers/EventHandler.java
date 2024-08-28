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
import com.ericsson.component.aia.mediation.netty.protocol.parser.MuxEvent;
import com.ericsson.component.aia.mediation.parsers.streamrecord.StreamedRecord;

/**
 * This class is responsible for handling the stream messages.
 *
 * @since 1.0.7
 *
 **/
public class EventHandler extends AbstractStreamHandler {

    @Override
    public void handle(final AbstractMuxEvent event) {
        final int sourceId = event.sourceId();
        if (VALID_SRC_SET.contains(sourceId)) {
            handleEvent((MuxEvent) event);
            incrementEvents();
        } else {
            if (!INVALID_SRC_SET.contains(sourceId)) {
                LOGGER.error("Got message for invalid sourceId {} ", sourceId);
                INVALID_SRC_SET.add(sourceId);
            }
            noSourceId();
        }
    }

    /**
     * This method handles an event received on a stream
     *
     * @param event
     * @throws Exception
     */
    protected void handleEvent(final MuxEvent event) {
        // Holder for the incoming record
        final int sourceId = event.sourceId();
        final StreamedRecord streamedRecord = new StreamedRecord(sourceId);
        streamedRecord.setRemoteIP(SOURCEID_IPADDRESS.get(Integer.valueOf(sourceId)));
        streamedRecord.setAction(StreamedRecord.Actions.EVENT);
        final ByteBuf buff = event.payload();
        final byte[] payload = new byte[buff.capacity()];
        buff.getBytes(0, payload);
        streamedRecord.setData(payload);
        offer(streamedRecord);
    }
}
