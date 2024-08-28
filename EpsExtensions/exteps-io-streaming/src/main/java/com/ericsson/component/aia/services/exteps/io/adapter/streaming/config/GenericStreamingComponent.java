/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.component.aia.services.exteps.io.adapter.streaming.config;

import com.ericsson.component.aia.mediation.netty.component.AbstractComponent;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.listener.GenericStreamingListener;

import io.netty.channel.ChannelHandler;

/**
 * This class defines the TOR streaming component that attaches to the end of the PM client stream.
 * See META-INF/services/com.ericsson.component.aia.mediation.engine.netty.component.Component for the definition of this
 * component for Java SPI
 *
 * @since 1.0.7
 */
public class GenericStreamingComponent extends AbstractComponent {

    private static final String VERSION = "1.0";
    private static final GenericStreamingListener streamingHandler = new GenericStreamingListener();

    /**
     * Constructor, instantiate the component and define its life cycle
     */
    public GenericStreamingComponent() {
        super(VERSION);
    }

    /**
     * Return the object that handles the channel interface: connects, disconnects, and message reception on the channel
     */
    @Override
    public ChannelHandler getHandler() {
        return streamingHandler;
    }

}
