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

import static io.netty.buffer.Unpooled.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import io.netty.buffer.ByteBuf;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.component.aia.mediation.netty.protocol.parser.MuxEvent;
import com.ericsson.component.aia.mediation.parsers.streamrecord.StreamedRecord;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.controller.StreamingController;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.listener.StreamingListener;

@RunWith(MockitoJUnitRunner.class)
public class EventHandlerTest {

    @Mock
    private MuxEvent mockMuxEvent;

    @Mock
    StreamingController mockstreamingController;

    private MyEventHandler myEventHandler;

    private int sourceId = 1;

    private ByteBuf buf;

    @Before
    public void setup() {
        StreamingListener.setController(mockstreamingController);
        doReturn(null).when(mockstreamingController).getStreamListener();
        myEventHandler = new MyEventHandler();
        MyEventHandler.VALID_SRC_SET.clear();
        MyEventHandler.INVALID_SRC_SET.clear();
    }

    @Test
    public void testHandle_whenContainsValidSourceId_verifyEventsIncremented() {
        MyEventHandler.VALID_SRC_SET.add(sourceId);
        doReturn(sourceId).when(mockMuxEvent).sourceId();
        final long count = myEventHandler.getReceivedMetrics().getEvents();
        myEventHandler.handle(mockMuxEvent);
        assertFalse(MyEventHandler.INVALID_SRC_SET.contains(sourceId));
        assertEquals(count + 1, myEventHandler.getReceivedMetrics().getEvents());
    }

    @Test
    public void testHandle_whenNotContainsValidSourceId_thenAddToInvalidSrcSet() {
        sourceId = 2;
        doReturn(sourceId).when(mockMuxEvent).sourceId();
        final long count = myEventHandler.getReceivedMetrics().getNoSrc();
        assertFalse(MyEventHandler.INVALID_SRC_SET.contains(sourceId));
        myEventHandler.handle(mockMuxEvent);
        assertFalse(MyEventHandler.VALID_SRC_SET.contains(sourceId));
        assertTrue(MyEventHandler.INVALID_SRC_SET.contains(sourceId));
        assertEquals(count + 1, myEventHandler.getReceivedMetrics().getNoSrc());
    }

    @Test
    public void testHandleEvent() {
        prepareEvent();
        myEventHandler.handleEvent(mockMuxEvent);
        final StreamedRecord streamedRecord = myEventHandler.getStreamedRecord();
        assertEquals(StreamedRecord.Actions.EVENT, streamedRecord.getAction());
        assertEquals(buf.array().length, streamedRecord.getData().length);
    }

    public void prepareEvent() {
        sourceId = 4;
        buf = buffer(4);
        buf.setInt(0, 1);
        doReturn(sourceId).when(mockMuxEvent).sourceId();
        doReturn(buf).when(mockMuxEvent).payload();
    }

    public class MyEventHandler extends EventHandler {

        private StreamedRecord streamedRecord;

        public MyEventHandler() {
            getReceivedMetrics().setMonitorOn(true);
        }

        @Override
        protected void handleEvent(final MuxEvent event) {
            if (event.sourceId() == 4) {
                super.handleEvent(event);
            } else {
                return;
            }
        }

        @Override
        public void offer(final StreamedRecord streamedRecord) {
            this.streamedRecord = streamedRecord;
        }

        public StreamedRecord getStreamedRecord() {
            return streamedRecord;
        }
    }
}
