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
package com.ericsson.component.aia.services.exteps.io.adapter.streaming.utils.torserver;

import static com.ericsson.component.aia.services.exteps.io.adapter.streaming.utils.torserver.StreamOutProtocol.*;

import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;

public class TORStreamingServerHandler extends SimpleChannelHandler {
    final private byte [] eventMessage;
    public TORStreamingServerHandler(final byte [] eventMessage){
        this.eventMessage = eventMessage;
    }
    @Override
    public void channelConnected(final ChannelHandlerContext ctx, final ChannelStateEvent event){
        final Channel channel = event.getChannel();
        ChannelBuffer channelBuffer = ChannelBuffers.wrappedBuffer(getInitilizationMessage());
        channel.write(channelBuffer);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }
        channelBuffer = ChannelBuffers.wrappedBuffer(getConnectionMessage());
        channel.write(channelBuffer);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }
        channelBuffer = ChannelBuffers.wrappedBuffer(eventMessage);
        channel.write(channelBuffer);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }
        channelBuffer = ChannelBuffers.wrappedBuffer(getDisconnectMessage());
        channel.write(channelBuffer);

    }

    @Override
    public void channelDisconnected(final ChannelHandlerContext ctx, final ChannelStateEvent event) throws IOException  {
        try {
            super.channelDisconnected(ctx, event);
        } catch (Exception exception) {
            throw new IOException(exception);
        }
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final ExceptionEvent event) {

        event.getCause().printStackTrace();

        final Channel channel = event.getChannel();
        channel.close();
    }


}
