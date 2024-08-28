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
package com.ericsson.component.aia.services.exteps.io.adapter.streaming.config;

/**
 * This class defines the TOR streaming component that attaches to the end of the PM client stream.
 * See META-INF/services/com.ericsson.component.aia.mediation.engine.netty.component.Component for the definition of this
 * component for Java SPI
 *
 */

import io.netty.channel.ChannelHandler;

import com.ericsson.component.aia.mediation.netty.component.AbstractComponent;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.listener.StreamingListener;

/**
 * This class is used register Listener which later will be used by netty engine to send events.
 *
 * @since 1.0.7
 *
 */
public class StreamingComponent extends AbstractComponent {

    private static final String VERSION = "1.0";
    private static final StreamingListener streamingHandler = new StreamingListener();

    /**
     * Constructor, instantiate the component and define its life cycle
     */
    public StreamingComponent() {
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
