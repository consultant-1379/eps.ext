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
 * This class defines the DatapathProvider used by the PM client stream.
 * See META-INF/services/com.ericsson.component.aia.mediation.engine.netty.DatapathProvider for the definition of this
 * component for Java SPI
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.mediation.netty.DatapathProvider;
import com.ericsson.component.aia.mediation.netty.configuration.Components;
import com.ericsson.component.aia.mediation.netty.configuration.DatapathConfiguration;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.StreamingConfig;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.controller.StreamingController;

public class StreamingDatapathProvider implements DatapathProvider {

    private static StreamingController streamingController;
    protected final Logger log = LoggerFactory.getLogger(getClass());

    public static void setController(final StreamingController controller) {
        streamingController = controller;
    }

    @Override
    public DatapathConfiguration getDatapathDefinition(final String datapathName) {
        final DatapathConfiguration datapathConfiguration = new DatapathConfiguration();
        datapathConfiguration.setName(datapathName);
        final StreamingConfig config = streamingController.getConfig();
        final Components components = getComponents(config);
        datapathConfiguration.setComponents(components);
        datapathConfiguration.setAddress(config.getHost());
        datapathConfiguration.setPort(config.getPort());
        datapathConfiguration.setTransport(config.getTransportName());
        datapathConfiguration.setWorkerExecutorReference(ComponentArray.getWorkerExecutorReferenceName());
        log.debug("datapath definition: {}, {}, {}", datapathConfiguration.getName(), datapathConfiguration.getAddress(),
                datapathConfiguration.getPort());
        return datapathConfiguration;
    }

    private Components getComponents(final StreamingConfig config) {
        final String[] componentArray = config.getComponentArray();
        final Components components = new Components();

        if (componentArray.length == 0) {
            components.setComponent(ComponentArray.getComponents(config.isStatisticsOn()));
        } else {
            components.setComponent(componentArray);
        }
        return components;
    }
}
