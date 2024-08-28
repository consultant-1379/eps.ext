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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.component.aia.mediation.netty.configuration.DatapathConfiguration;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.StreamingConfig;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.controller.StreamingController;

@RunWith(MockitoJUnitRunner.class)
public class StreamingDatapathProviderTest {

    private StreamingDatapathProvider streamingDatapathProvider;

    @Mock
    private StreamingController mockStreamingController;

    @Mock
    private StreamingConfig mockStreamingConfig;

    String host = "1.1.1.1";
    int port = 8080;
    String transportName = "NIO_SOCKET_CLIENT";

    boolean statisticsOn = false;

    private final String[] expectedLoadedComponentArrayFromFlowXml = {
            "com.ericsson.component.aia.mediation.netty.component.multiplex.decoder.MultiplexDecoderComponent",
            "com.ericsson.component.aia.mediation.netty.component.multiplex.handshake.MultiplexClientHandshakeComponent",
            "com.ericsson.component.aia.services.exteps.io.adapter.streaming.config.GenericStreamingComponent",
            "com.ericsson.component.aia.mediation.netty.component.discard.ReleaseBufferComponent" };

    @Before
    public void setup() {
        streamingDatapathProvider = new StreamingDatapathProvider();
        StreamingDatapathProvider.setController(mockStreamingController);
    }

    @Test
    public void testGetDatapathDefinition() {
        final String datapathName = "Anything";
        doReturn(mockStreamingConfig).when(mockStreamingController).getConfig();
        doReturn(host).when(mockStreamingConfig).getHost();
        doReturn(port).when(mockStreamingConfig).getPort();
        doReturn(transportName).when(mockStreamingConfig).getTransportName();
        final String[] componentArrayContensIfNotPresentInPropertiesFile = StreamingConfig.NO_COMPONENT_ARRAY_DEFINED_BY_USER;
        doReturn(componentArrayContensIfNotPresentInPropertiesFile).when(mockStreamingConfig).getComponentArray();
        doReturn(statisticsOn).when(mockStreamingConfig).isStatisticsOn();
        final DatapathConfiguration dataPathConf = streamingDatapathProvider.getDatapathDefinition(datapathName);
        for (int i = 0; i < ComponentArray.getComponents(statisticsOn).length; i++) {
            assertEquals(ComponentArray.getComponents(statisticsOn)[i], dataPathConf.getComponents().getComponent()[i]);
        }
        assertEquals(host, dataPathConf.getAddress());
        assertEquals(port, dataPathConf.getPort());
        assertEquals(ComponentArray.getTransportName(), dataPathConf.getTransport());
        assertEquals(ComponentArray.getWorkerExecutorReferenceName(), dataPathConf.getWorkerExecutorReference());
    }

    @Test
    public void testGetDatapathDefinitionLoadingComponentsFromFlowXml() {
        final String datapathName = "Anything";
        doReturn(mockStreamingConfig).when(mockStreamingController).getConfig();
        doReturn(expectedLoadedComponentArrayFromFlowXml).when(mockStreamingConfig).getComponentArray();
        final DatapathConfiguration dataPathConf = streamingDatapathProvider.getDatapathDefinition(datapathName);
        for (int index = 0; index < expectedLoadedComponentArrayFromFlowXml.length; index++) {
            assertEquals(expectedLoadedComponentArrayFromFlowXml[index], dataPathConf.getComponents().getComponent()[index]);
        }
    }
}
