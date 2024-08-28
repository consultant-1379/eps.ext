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

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;

public class TORStreamingServerFactory implements ChannelPipelineFactory {
    private byte[] eventMessage;
    @Override
    public ChannelPipeline getPipeline() {
        // Create and configure a new pipeline for a new channel.
        return Channels.pipeline(new TORStreamingServerHandler(eventMessage));
    }
    public TORStreamingServerFactory(final byte []eventMessage){
        this.eventMessage = eventMessage;
    }
    public TORStreamingServerFactory(){
        
    }
}
