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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.component.aia.mediation.netty.protocol.parser.MuxDropped;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.controller.StreamingController;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.listener.StreamingListener;

@RunWith(MockitoJUnitRunner.class)
public class DroppedEventHandlerTest {

    private MyDroppedEventHandler myDroppedEventHandler;

    @Mock
    private MuxDropped mockMuxDropped;

    @Mock
    StreamingController mockstreamingController;

    @Before
    public void setup() {
        StreamingListener.setController(mockstreamingController);
        doReturn(null).when(mockstreamingController).getStreamListener();
        myDroppedEventHandler = new MyDroppedEventHandler();
    }

    @Test
    public void testHandl_verifyIncrementDrops() {
        final long drops = myDroppedEventHandler.getReceivedMetrics().getDrops();
        myDroppedEventHandler.handle(mockMuxDropped);
        assertEquals(drops + 1, myDroppedEventHandler.getReceivedMetrics().getDrops());
    }

    @Test
    public void testHandleEventsDropped() {
        final long NUM_EVENTS = 10;
        doReturn(NUM_EVENTS).when(mockMuxDropped).count();
        final long dropped = myDroppedEventHandler.getReceivedMetrics().getLostRecords();
        myDroppedEventHandler.handleEventsDropped(mockMuxDropped);
        assertEquals(dropped + NUM_EVENTS, myDroppedEventHandler.getReceivedMetrics().getLostRecords());

    }

    public class MyDroppedEventHandler extends DroppedEventHandler {

        public MyDroppedEventHandler() {
            getReceivedMetrics().setMonitorOn(true);
        }
    }

}
