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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.io.Serializable;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.component.aia.mediation.netty.component.multiplex.handshake.configuration.MultiplexClientHandshakeConfiguration;
import com.ericsson.component.aia.mediation.netty.configuration.EngineConfiguration;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.StreamingConfig;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.controller.StreamingController;

@RunWith(MockitoJUnitRunner.class)
public class StreamingConfigurationProviderTest {

    @Spy
    private StreamingConfigurationProvider spyConfigurationProvider;

    @Mock
    private StreamingConfig mockStreamingConfig;

    @Mock
    private StreamingController mockStreamingController;

    static final int userId = 1;
    static final int filterId = 2;
    static final int groupId = 3;

    @Before
    public void setup() {
        StreamingConfigurationProvider.setController(mockStreamingController);
    }

    @Test
    public void testGetComponentConfiguration_ReturnDefaultConfiguration() {

        final TestDefaultComponentConfiguration testComponentConfiguration = new TestDefaultComponentConfiguration();
        final Object componentConfiguration = spyConfigurationProvider
                .getComponentConfiguration("AnyValue", 0, testComponentConfiguration.getClass());

        assertTrue(componentConfiguration instanceof TestDefaultComponentConfiguration);
    }

    @Test
    public void testGetComponentConfiguration_ReturnMultiplexClientHandshakeConfiguration() {
        final MultiplexClientHandshakeConfiguration multiplexClientHandshakeConfiguration = new MultiplexClientHandshakeConfiguration();
        doReturn(userId).when(mockStreamingConfig).getUserId();
        doReturn(filterId).when(mockStreamingConfig).getFilterId();
        doReturn(groupId).when(mockStreamingConfig).getGroupId();
        doReturn(mockStreamingConfig).when(mockStreamingController).getConfig();
        final Object componentConfiguration = spyConfigurationProvider.getComponentConfiguration("AnyValue", 0,
                multiplexClientHandshakeConfiguration.getClass());
        assertTrue(componentConfiguration instanceof MultiplexClientHandshakeConfiguration);
        assertEquals(userId, ((MultiplexClientHandshakeConfiguration) componentConfiguration).getUid());
        assertEquals(filterId, ((MultiplexClientHandshakeConfiguration) componentConfiguration).getFid());
        assertEquals(groupId, ((MultiplexClientHandshakeConfiguration) componentConfiguration).getGid());
    }

    @Test(expected = Exception.class)
    public void testGetComponentConfiguration_ThrowException() {
        final MultiplexClientHandshakeConfiguration multiplexClientHandshakeConfiguration = new MultiplexClientHandshakeConfiguration();
        doThrow(new InterruptedException()).when(mockStreamingController).getConfig();
        spyConfigurationProvider.getComponentConfiguration("AnyValue", 0, multiplexClientHandshakeConfiguration.getClass());
    }

    @Test
    public void testGetEngineConfiguration() {
        final EngineConfiguration engineConfiguration = spyConfigurationProvider.getEngineConfiguration();
        assertEquals(ComponentArray.getExecutorGroupCount(), engineConfiguration.getExecutorConfiguration().getEngineExecutorGroup()[0].getCount());
        assertEquals(ComponentArray.getExecutorGroupName(), engineConfiguration.getExecutorConfiguration().getEngineExecutorGroup()[0].getName());
        assertEquals(ComponentArray.getExtensionType(), engineConfiguration.getExtensions().get(0).getType());
    }
}

class TestDefaultComponentConfiguration implements Serializable {

    private static final long serialVersionUID = -523358006949925219L;

}
