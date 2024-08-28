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
 * This class defines the ConfigurationProvider used by the PM client stream.
 * See META-INF/services/com.ericsson.component.aia.mediation.engine.netty.ConfigurationProvider for the definition of this
 * component for Java SPI
 *
 */

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.mediation.netty.ConfigurationProvider;
import com.ericsson.component.aia.mediation.netty.EngineConfigurationException;
import com.ericsson.component.aia.mediation.netty.component.multiplex.handshake.configuration.MultiplexClientHandshakeConfiguration;
import com.ericsson.component.aia.mediation.netty.configuration.*;
import com.ericsson.component.aia.mediation.netty.extension.metrics.registry.configuration.NettyRegistryConfiguration;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.StreamingConfig;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.controller.StreamingController;

/**
 * This class is responsible for providing configuration for netty engine
 *
 * @since 1.0.7
 *
 */
public class StreamingConfigurationProvider implements ConfigurationProvider {

    private static final Logger logger = LoggerFactory.getLogger(StreamingConfigurationProvider.class);
    private static StreamingController streamingController;

    public static void setController(final StreamingController controller) {
        streamingController = controller;
    }

    /**
     * This method is responsible for returning the configuration for the netty component.
     */
    @Override
    public <ComponentConfiguration extends Serializable> ComponentConfiguration getComponentConfiguration(final String dataPathName,
                                                                                                          final int componentId,
                                                                                                          final Class<ComponentConfiguration> component) {
        if (!component.equals(MultiplexClientHandshakeConfiguration.class)) {
            return getDefaultConfiguration(component);
        }
        final MultiplexClientHandshakeConfiguration mcc = new MultiplexClientHandshakeConfiguration();
        try {
            final StreamingConfig config = streamingController.getConfig();
            mcc.setUid((short) config.getUserId());
            mcc.setFid((short) config.getFilterId());
            mcc.setGid(config.getGroupId());

        } catch (final Exception e) {
            logger.warn("could not set filter, group, and user properties from configuration for TOR streaming", e);
        }
        logger.debug("multiplex client definition: {} , {} , {} ", mcc.getFid(), mcc.getGid(), mcc.getUid());
        return (ComponentConfiguration) mcc;
    }

    /**
     * Allows Subscription for reconfiguration events on the data path, subscriptions are not supported here
     *
     * @param dataPathName
     *        : The data path the component is on
     * @param component
     *        ID: The ID of the component instance
     * @param type
     *        : The type of configuration class to return
     */
    @Override
    public <ComponentConfiguration extends Serializable> void subscribeForReconfiguration(final String datapathName, final int componentId,
                                                                                          final Class<ComponentConfiguration> type) {
    }

    /**
     * Gets the engine configuration, the default engine configuration is returned
     *
     * @return THe default engine configuration
     */
    @Override
    public EngineConfiguration getEngineConfiguration() {
        final EngineExecutorGroup engineExecutorGroup = new EngineExecutorGroup();
        engineExecutorGroup.setCount(ComponentArray.getExecutorGroupCount());
        engineExecutorGroup.setName(ComponentArray.getExecutorGroupName());
        final ExecutorConfiguration executorConfiguration = new ExecutorConfiguration();
        executorConfiguration.setEngineExecutorGroup(new EngineExecutorGroup[] { engineExecutorGroup });
        final EnabledExtension enabledExtension = new EnabledExtension();
        enabledExtension.setType(ComponentArray.getExtensionType());
        enabledExtension.setConfiguration(new NettyRegistryConfiguration());
        final List<EnabledExtension> extensionList = new ArrayList<>();
        extensionList.add(enabledExtension);
        final EngineConfiguration configuration = this.getDefaultConfiguration(EngineConfiguration.class);
        configuration.setExtensions(extensionList);
        configuration.setExecutorConfiguration(executorConfiguration);
        return configuration;
    }

    /**
     * Method to return the default configuration (default constructor on the type) for the given class type
     *
     * @param type
     *        The configuration class type
     * @return The default configuration
     */
    private <ComponentConfiguration> ComponentConfiguration getDefaultConfiguration(final Class<ComponentConfiguration> type) {
        try {
            return type.newInstance();
        } catch (final Exception e) {
            throw new EngineConfigurationException("Can't create configuration for " + type + " reason: " + e.getMessage());
        }
    }
}
