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

import com.ericsson.component.aia.mediation.netty.protocol.parser.MuxConnection;
import com.ericsson.component.aia.mediation.parsers.streamrecord.StreamedRecord;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.controller.StreamingController;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.listener.StreamingListener;

@RunWith(MockitoJUnitRunner.class)
public class ConnectionHandlerTest {

    @Mock
    private MuxConnection mockMuxConnection;

    @Mock
    StreamingController mockstreamingController;

    private MyConnectionHandler myConnectionHandler;

    private int sourceId = 1;

    private static final byte[] IP_ADDR = new byte[] { 1, 1, 1, 1 };;

    private static final ByteBuf buf = buffer(4);

    @Before
    public void setup() {
        StreamingListener.setController(mockstreamingController);
        doReturn(null).when(mockstreamingController).getStreamListener();
        myConnectionHandler = new MyConnectionHandler();
        MyConnectionHandler.VALID_SRC_SET.add(sourceId);
    }

    @Test
    public void testHandle_verifyIncrementConnects() {
        doReturn(sourceId).when(mockMuxConnection).sourceId();
        final long connects = myConnectionHandler.getReceivedMetrics().getConnects();
        myConnectionHandler.handle(mockMuxConnection);
        assertEquals(connects + 1, myConnectionHandler.getReceivedMetrics().getConnects());
    }

    @Test
    public void testCheckValidSourceId_forNewSourceId_addToValidSourceIdList() {
        sourceId = 2;
        assertFalse(MyConnectionHandler.VALID_SRC_SET.contains(sourceId));
        myConnectionHandler.checkValidSoruceId(sourceId);
        assertTrue(MyConnectionHandler.VALID_SRC_SET.contains(sourceId));
    }

    @Test
    public void testCheckValidSourceId_forNewSourceId_removeFromInvalidSourceId() {
        sourceId = 3;
        MyConnectionHandler.INVALID_SRC_SET.add(sourceId);
        myConnectionHandler.checkValidSoruceId(sourceId);
        assertFalse(MyConnectionHandler.INVALID_SRC_SET.contains(sourceId));
    }

    @Test
    public void testHandleConnect() {
        prepareEvent();
        myConnectionHandler.handleConnect(mockMuxConnection);
        final StreamedRecord streamedRecord = myConnectionHandler.getStreamedRecord();
        assertEquals(StreamedRecord.Actions.CONNECT, streamedRecord.getAction());
        assertEquals(IP_ADDR, streamedRecord.getRemoteIP());
        assertEquals(IP_ADDR, MyConnectionHandler.SOURCEID_IPADDRESS.get(sourceId));
        for (int i = 0; i < buf.array().length; i++) {
            assertEquals(buf.array()[i], streamedRecord.getData()[i]);
        }
    }

    public void prepareEvent() {
        sourceId = 4;
        buf.setInt(0, 1);
        doReturn(sourceId).when(mockMuxConnection).sourceId();
        doReturn(IP_ADDR).when(mockMuxConnection).ip();
        doReturn(buf).when(mockMuxConnection).payload();
    }

    public class MyConnectionHandler extends ConnectionHandler {

        private StreamedRecord streamedRecord;

        public MyConnectionHandler() {
            getReceivedMetrics().setMonitorOn(true);
        }

        @Override
        protected void handleConnect(final MuxConnection event) {
            if (event.sourceId() == 1) {
                return;
            } else {
                super.handleConnect(event);
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
