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
package com.ericsson.component.aia.services.exteps.io.adapter.streaming.listener;

import static io.netty.buffer.Unpooled.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.component.aia.mediation.netty.protocol.StreamOutProtocol.EventType;
import com.ericsson.component.aia.mediation.netty.protocol.parser.DefaultStreamOutProtocolParser;
import com.ericsson.component.aia.mediation.netty.protocol.parser.MuxEvent;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.StreamingConfig;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.controller.StreamingController;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.controller.StreamingState;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.handlers.EventHandler;

@RunWith(MockitoJUnitRunner.class)
public class StreamingListenerTest {

    @Mock
    private ChannelHandlerContext mockChannelHandlerContext;

    @Mock
    private MuxEvent mockMuxEvent;

    @Mock
    private EventHandler mockEventHandler;

    @Mock
    private DefaultStreamOutProtocolParser mockDefaultStreamOutProtocolParser;

    private StreamingListener spyStreamingListener;

    private StreamingListener streamingListener;

    @Before
    public void setup() {
        final StreamingConfig streamingConfig = new StreamingConfig();
        final RecordListener recordListener = null;
        final StreamingController streamingController = new StreamingController(recordListener);
        streamingController.start(streamingConfig);
        streamingController.getConfig().setMonitorOn(true);
        streamingController.getConfig().setMonitorPeriod(1000);
        streamingListener = new StreamingListener();
        spyStreamingListener = spy(streamingListener);
    }

    @Test
    public void testChannelInactive_verifyStreamingStateToReset() {
        StreamingListener.getController().setStreamingState(StreamingState.Connecting);
        spyStreamingListener.channelInactive(mockChannelHandlerContext);
        verify(mockChannelHandlerContext, times(1)).fireChannelInactive();
        assertEquals(StreamingState.Resetting, StreamingListener.getController().getStreamingState());
    }

    @Test
    public void testChannelActive_verifyStreamingStateToConnected() {
        StreamingListener.getController().setStreamingState(StreamingState.Disconnected);
        spyStreamingListener.channelActive(mockChannelHandlerContext);
        verify(mockChannelHandlerContext, times(1)).fireChannelActive();
        assertEquals(StreamingState.Disconnected, StreamingListener.getController().getStreamingState());
        StreamingListener.getController().setStreamingState(StreamingState.Connecting);
        spyStreamingListener.channelActive(mockChannelHandlerContext);
        assertEquals(StreamingState.Connected, StreamingListener.getController().getStreamingState());
    }

    @Test
    public void testExceptionCaught_verifyStreamingStateToReset() {
        StreamingListener.getController().setStreamingState(StreamingState.Connecting);
        spyStreamingListener.exceptionCaught(mockChannelHandlerContext, new IllegalStateException());
        verify(mockChannelHandlerContext, times(1)).close();
        assertEquals(StreamingState.Resetting, StreamingListener.getController().getStreamingState());
    }

    @Test
    public void testChannelReadChannelHandlerContextObject_whenHandlerIsNULL() {
        final ByteBuf buf = setExpectationReturnBuffer();
        doReturn(null).when(spyStreamingListener).getHandler(EventType.PAYLOAD);
        final long invalidRecCount = spyStreamingListener.getReceivedMetrics().getInvalidRecords();
        spyStreamingListener.channelRead(mockChannelHandlerContext, buf);
        verify(mockEventHandler, times(0)).handle(mockMuxEvent);
        assertEquals(invalidRecCount + 1, spyStreamingListener.getReceivedMetrics().getInvalidRecords());
    }

    @Test
    public void testChannelReadChannelHandlerContextObject() {
        final ByteBuf buf = setExpectationReturnBuffer();
        doReturn(mockEventHandler).when(spyStreamingListener).getHandler(EventType.PAYLOAD);
        final long recordCount = spyStreamingListener.getReceivedMetrics().getRecords();
        spyStreamingListener.channelRead(mockChannelHandlerContext, buf);
        verify(mockEventHandler, times(1)).handle(mockMuxEvent);
        assertEquals(recordCount + 1, spyStreamingListener.getReceivedMetrics().getRecords());
    }

    protected ByteBuf setExpectationReturnBuffer() {
        final ByteBuf buf = buffer(4);
        buf.setInt(0, 1);
        doReturn(EventType.PAYLOAD).when(mockMuxEvent).eventType();
        doReturn(mockDefaultStreamOutProtocolParser).when(spyStreamingListener).getParser();
        doReturn(mockMuxEvent).when(mockDefaultStreamOutProtocolParser).parse(buf);
        doNothing().when(mockEventHandler).handle(mockMuxEvent);
        return buf;
    }
}
