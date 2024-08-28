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

import com.ericsson.component.aia.mediation.netty.protocol.StreamOutProtocol.DisconnectionReason;
import com.ericsson.component.aia.mediation.netty.protocol.parser.MuxDisconnection;
import com.ericsson.component.aia.mediation.parsers.streamrecord.StreamedRecord;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.controller.StreamingController;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.listener.StreamingListener;

@RunWith(MockitoJUnitRunner.class)
public class DisConnectionHandlerTest {

    @Mock
    private MuxDisconnection mockMuxDisconnection;

    @Mock
    StreamingController mockstreamingController;

    private MyDisConnectionHandler myDisConnectionHandler;

    private int sourceId = 1;

    private static final byte[] IP_ADDR = new byte[] { 1, 1, 1, 1 };

    @Before
    public void setup() {
        StreamingListener.setController(mockstreamingController);
        doReturn(null).when(mockstreamingController).getStreamListener();
        myDisConnectionHandler = new MyDisConnectionHandler();
        myDisConnectionHandler.getReceivedMetrics().setMonitorOn(true);
        MyDisConnectionHandler.VALID_SRC_SET.add(sourceId);
    }

    @Test
    public void testHandle_verifyIncrementDisconnects() {
        doReturn(sourceId).when(mockMuxDisconnection).sourceId();
        final long disconnects = myDisConnectionHandler.getReceivedMetrics().getDisconnects();
        myDisConnectionHandler.handle(mockMuxDisconnection);
        assertEquals(disconnects + 1, myDisConnectionHandler.getReceivedMetrics().getDisconnects());
    }

    @Test
    public void testCheckValidSoruceId_forExisting_removeFromValidSourceId_returnTrue() {
        sourceId = 3;
        MyDisConnectionHandler.VALID_SRC_SET.add(sourceId);
        final boolean result = myDisConnectionHandler.checkValidSoruceId(sourceId);
        assertTrue(result);
        assertFalse(MyDisConnectionHandler.VALID_SRC_SET.contains(sourceId));
    }

    @Test
    public void testCheckValidSoruceId_forExisting_removeFromInvalidSourceId_returnTrue() {
        sourceId = 4;
        MyDisConnectionHandler.INVALID_SRC_SET.add(sourceId);
        final boolean result = myDisConnectionHandler.checkValidSoruceId(sourceId);
        assertTrue(result);
        assertFalse(MyDisConnectionHandler.VALID_SRC_SET.contains(sourceId));
        assertFalse(MyDisConnectionHandler.INVALID_SRC_SET.contains(sourceId));
    }

    @Test
    public void testCheckValidSoruceId_forNonExisting_returnFalse() {
        sourceId = 5;
        final boolean result = myDisConnectionHandler.checkValidSoruceId(sourceId);
        assertFalse(result);
    }

    @Test
    public void testHandleConnect_validEvent_StreamedRecordCorrectlySet() {
        prepareEvent();
        myDisConnectionHandler.handleDisconnect(mockMuxDisconnection);
        final StreamedRecord streamedRecord = myDisConnectionHandler.getStreamedRecord();
        assertEquals(sourceId, streamedRecord.getSourceId());
        assertEquals(StreamedRecord.Actions.DISCONNECT, streamedRecord.getAction());
        assertEquals(IP_ADDR, streamedRecord.getRemoteIP());
        assertEquals(DisconnectionReason.NORMAL.reason(), streamedRecord.getDisconnectReason());
    }

    public void prepareEvent() {
        sourceId = 6;
        doReturn(sourceId).when(mockMuxDisconnection).sourceId();
        doReturn(DisconnectionReason.NORMAL).when(mockMuxDisconnection).reason();
        MyDisConnectionHandler.SOURCEID_IPADDRESS.put(sourceId, IP_ADDR);
    }

    public class MyDisConnectionHandler extends DisConnectionHandler {

        private StreamedRecord streamedRecord;

        public MyDisConnectionHandler() {
            getReceivedMetrics().setMonitorOn(true);
        }

        @Override
        protected void handleDisconnect(final MuxDisconnection event) {
            if (event.sourceId() == 1) {
                return;
            } else {
                super.handleDisconnect(event);
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
