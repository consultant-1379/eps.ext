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

import com.ericsson.component.aia.mediation.netty.protocol.StreamOutProtocol;
import com.ericsson.component.aia.mediation.netty.protocol.parser.AbstractMuxEvent;
import com.ericsson.component.aia.mediation.netty.protocol.parser.MuxDisconnection;
import com.ericsson.component.aia.mediation.parsers.streamrecord.StreamedRecord;

/**
 * This class is responsible for handling the stream disconnection messages.
 *
 * @since 1.0.7
 *
 **/
public class DisConnectionHandler extends AbstractStreamHandler {

    @Override
    public void handle(final AbstractMuxEvent event) {
        final int sourceId = event.sourceId();
        if (checkValidSoruceId(sourceId)) {
            handleDisconnect((MuxDisconnection) event);
            incrementDisconnects();
        }
    }

    protected boolean checkValidSoruceId(final int sourceId) {
        if (VALID_SRC_SET.contains(sourceId)) {
            VALID_SRC_SET.remove(sourceId);
            LOGGER.trace("Removing {} from valid source set", sourceId);
        } else if (INVALID_SRC_SET.contains(sourceId)) {
            INVALID_SRC_SET.remove(sourceId);
            LOGGER.trace("Removing {} from invalid source set", sourceId);
        } else {
            LOGGER.warn("Got disconnect for unknown sourceID {} ", sourceId);
            return false;
        }
        return true;
    }

    /**
     * This method handles disconnection of a node on an event stream
     *
     * @param event
     */
    protected void handleDisconnect(final MuxDisconnection event) {
        final int sourceId = event.sourceId();
        final byte[] ipAddress = SOURCEID_IPADDRESS.get(Integer.valueOf(sourceId));
        final StreamedRecord streamedRecord = new StreamedRecord(sourceId);
        streamedRecord.setAction(StreamedRecord.Actions.DISCONNECT);
        streamedRecord.setRemoteIP(ipAddress);
        final StreamOutProtocol.DisconnectionReason disconnectReason = event.reason();
        streamedRecord.setDisconnectReason(disconnectReason.reason());
        offer(streamedRecord);
        LOGGER.debug("Disconnect message for IP ADDRESS :: {}", ipAddress);
    }
}
