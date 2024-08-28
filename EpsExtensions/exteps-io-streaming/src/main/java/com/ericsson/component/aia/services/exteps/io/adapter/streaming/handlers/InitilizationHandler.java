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

import com.ericsson.component.aia.mediation.netty.protocol.parser.AbstractMuxEvent;
import com.ericsson.component.aia.mediation.netty.protocol.parser.MuxInit;

public class InitilizationHandler extends AbstractStreamHandler {

    @Override
    public void handle(final AbstractMuxEvent event) {
        handleInitiation((MuxInit) event);
    }

    /**
     * This method handles initiation of an event stream
     *
     * @param event
     */
    private void handleInitiation(final MuxInit event) {
        // The protocol version is 8 bytes long
        final long protocolVersion = event.protocolVersion();

        // Check the protocol versions match
        if (protocolVersion != PROTOCOL_VERSION) {
            LOGGER.debug("protocol version mismatch, sent {} ", PROTOCOL_VERSION, " received {} ", protocolVersion);
            LOGGER.debug("Expected  {}", Long.toBinaryString(PROTOCOL_VERSION));
            LOGGER.debug("Got {} ", Long.toBinaryString(protocolVersion));
        }
    }

}
