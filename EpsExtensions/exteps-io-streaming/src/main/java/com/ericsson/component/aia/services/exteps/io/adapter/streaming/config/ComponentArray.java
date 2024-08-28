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

public abstract class ComponentArray {

    private static String[] componentArray = { "com.ericsson.component.aia.mediation.netty.component.multiplex.decoder.MultiplexDecoderComponent",
        "com.ericsson.component.aia.mediation.netty.component.multiplex.handshake.MultiplexClientHandshakeComponent",
        "com.ericsson.component.aia.services.exteps.io.adapter.streaming.config.StreamingComponent",
        "com.ericsson.component.aia.mediation.netty.component.discard.ReleaseBufferComponent" };
    private static String[] componentArrayWithStatictics = {
        "com.ericsson.component.aia.mediation.netty.component.multiplex.decoder.MultiplexDecoderComponent",
        "com.ericsson.component.aia.mediation.netty.component.multiplex.handshake.MultiplexClientHandshakeComponent",
        "com.ericsson.component.aia.mediation.netty.component.statistics.StatisticsComponent",
        "com.ericsson.component.aia.services.exteps.io.adapter.streaming.config.StreamingComponent",
        "com.ericsson.component.aia.mediation.netty.component.discard.ReleaseBufferComponent" };

    /**
     * The list of components used in the netty stack.
     *
     * @return String[]
     */
    public static String[] getComponents(final boolean statisticsOn) {
        return statisticsOn ? componentArrayWithStatictics : componentArray;
    }

    /**
     *
     * @return
     */
    public static String getExtensionType() {
        return "com.ericsson.component.aia.mediation.netty.extension.metrics.registry.NettyMetricsRegistry";
    }

    /**
     *
     * @return
     */
    public static String getExecutorGroupName() {
        return "stream_client";
    }

    /**
     *
     * @return
     */
    public static String getWorkerExecutorReferenceName() {
        return "stream_client";
    }

    /**
     *
     * @return
     */
    public static String getTransportName() {
        return "NIO_SOCKET_CLIENT";
    }

    public static int getExecutorGroupCount() {
        return 1;
    }
}
