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
package com.ericsson.component.aia.services.exteps.io.adapter.streaming.controller;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.component.aia.mediation.netty.Engine;
import com.ericsson.component.aia.mediation.netty.EngineException;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.StreamingConfig;

@RunWith(MockitoJUnitRunner.class)
public class StreamingControllerTest {

    @Spy
    StreamingController spyStreamingController;

    @Mock
    Engine mockEngine;

    private final StringBuilder datapathName = new StringBuilder("StreamController_");

    @Before
    public void setup() {
        doNothing().when(spyStreamingController).waitForTransition();
        doReturn(mockEngine).when(spyStreamingController).getNettyEngine();
    }

    @Test
    public void testStart() {
        spyStreamingController.start(new StreamingConfig());
        assertEquals(StreamingState.Connecting, spyStreamingController.getStreamingState());
        spyStreamingController.stop();
    }

    @Test
    public void testReset() {
        spyStreamingController.reset();
        assertEquals(StreamingState.Disconnected, spyStreamingController.getStreamingState());
        spyStreamingController.setStreamingState(StreamingState.Connected);
        spyStreamingController.reset();
        assertEquals(StreamingState.Resetting, spyStreamingController.getStreamingState());
        spyStreamingController.stop();
    }

    @Test
    public void testConnected() {
        spyStreamingController.connected();
        assertEquals(StreamingState.Disconnected, spyStreamingController.getStreamingState());
        spyStreamingController.setStreamingState(StreamingState.Connecting);
        spyStreamingController.connected();
        assertEquals(StreamingState.Connected, spyStreamingController.getStreamingState());
    }

    @Test
    public void testStop() {
        spyStreamingController.start(new StreamingConfig());
        spyStreamingController.stop();
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        assertEquals(StreamingState.Disconnecting, spyStreamingController.getStreamingState());
    }

    @Test
    public void testHandleTransition_CaseDisconnected_ReturnNothing() {
        spyStreamingController.setStreamingState(StreamingState.Disconnected);
        spyStreamingController.handleTransition();
        verify(spyStreamingController).waitForTransition();
        assertEquals(StreamingState.Disconnected, spyStreamingController.getStreamingState());
    }

    @Test
    public void testHandleTransition_CaseConnecting_CallHandleConnect() throws InterruptedException {
        spyStreamingController.setStreamingState(StreamingState.Connecting);
        doNothing().when(spyStreamingController).handleConnect();
        spyStreamingController.handleTransition();
        verify(spyStreamingController, times(1)).handleConnect();
        verify(spyStreamingController, times(1)).waitForTransition();
    }

    @Test
    public void testHandleTransition_CaseConnected_ReturnNothing() {
        spyStreamingController.setStreamingState(StreamingState.Connected);
        spyStreamingController.handleTransition();
        verify(spyStreamingController, times(1)).waitForTransition();
        assertEquals(StreamingState.Connected, spyStreamingController.getStreamingState());
    }

    @Test
    public void testHandleTransition_CaseResetting_CallHandleReset() {
        spyStreamingController.setStreamingState(StreamingState.Resetting);
        doNothing().when(spyStreamingController).handleReset();
        spyStreamingController.handleTransition();
        verify(spyStreamingController, times(1)).handleReset();
        verify(spyStreamingController, times(1)).waitForTransition();
    }

    @Test
    public void testHandleTransition_CaseDisconnecting_CallHandleDisconnect() {
        spyStreamingController.setStreamingState(StreamingState.Disconnecting);
        doNothing().when(spyStreamingController).handleDisconnect();
        spyStreamingController.handleTransition();
        verify(spyStreamingController, times(1)).handleDisconnect();
        verify(spyStreamingController, times(1)).waitForTransition();
    }

    @Test
    public void testHandleDisconnect() {
        spyStreamingController.handleDisconnect();
        verify(mockEngine, times(1)).stopDataPath(datapathName.toString());
        verify(mockEngine, times(1)).stop();
        assertEquals(StreamingState.Disconnected, spyStreamingController.getStreamingState());
    }

    @Test
    public void testHandleReset() {
        spyStreamingController.handleReset();
        verify(mockEngine, times(1)).stopDataPath(datapathName.toString());
        verify(mockEngine, times(1)).stop();
        assertEquals(StreamingState.Connecting, spyStreamingController.getStreamingState());
    }

    @Test
    public void testHandleConnect_NoException() {
        spyStreamingController.handleConnect();
        verify(mockEngine, times(1)).start();
        verify(mockEngine, times(1)).startDataPath(datapathName.toString());
    }

    @Test
    public void testHandleConnect_ThrowException() {
        doReturn(mockEngine).when(spyStreamingController).getNettyEngine();
        doThrow(EngineException.class).when(mockEngine).startDataPath(datapathName.toString());
        spyStreamingController.handleConnect();
        verify(spyStreamingController, times(1)).reset();
    }
}
