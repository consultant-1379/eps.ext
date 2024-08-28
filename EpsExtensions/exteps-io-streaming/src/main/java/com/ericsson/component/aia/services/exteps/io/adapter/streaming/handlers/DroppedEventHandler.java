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
import com.ericsson.component.aia.mediation.netty.protocol.parser.MuxDropped;

/**
 * This class is responsible for handling the stream dropped messages.
 *
 * @since 1.0.7
 *
 **/
public class DroppedEventHandler extends AbstractStreamHandler {

    @Override
    public void handle(final AbstractMuxEvent event) {

        handleEventsDropped((MuxDropped) event);
        incrementDrops();
    }

    /**
     * This method handles events dropped from a node on an event stream
     *
     * @param event
     */
    protected void handleEventsDropped(final MuxDropped event) {
        lostRecords(event.count());
        LOGGER.debug("{} Events dropped Reason :: {}", event.count(), event.reason());
    }

}
