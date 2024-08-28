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

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class TORStreamingServer {

    private final int port;
    private final byte [] eventMessage;

    public TORStreamingServer(final int port) {
        this.port = port;
        this.eventMessage = new byte[]{0x00,0x00,0x00,0x01,0x00,0x0A,0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08};
    }
    
    public TORStreamingServer(final int port,final byte [] eventMessage) {
        this.port = port;
        this.eventMessage = eventMessage;
    }

    public static void main(final String[] args) {
        new TORStreamingServer(10866).start();
        new TORStreamingServer(10867).start();
    }

    public void start() {
        final Executor bossPool = Executors.newCachedThreadPool();
        final Executor workerPool = Executors.newCachedThreadPool();
        final ChannelFactory factory = new NioServerSocketChannelFactory(bossPool, workerPool);

        final ServerBootstrap bootstrap = new ServerBootstrap(factory);
        bootstrap.setPipelineFactory(new TORStreamingServerFactory(eventMessage));
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);

        bootstrap.bind(new InetSocketAddress(port));
    }
}
