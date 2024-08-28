/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2019
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.component.aia.services.exteps.io.adapter.ipl;

import com.ericsson.component.aia.itpf.common.config.Configuration;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.component.aia.services.eps.coordination.EpsAdaptiveConfiguration;
import com.ericsson.component.aia.services.exteps.io.adapter.util.GlobalPropertiesRetrieverUtil;

import org.junit.Test;
import org.powermock.reflect.Whitebox;

import static com.ericsson.component.aia.services.exteps.io.adapter.common.Constants.GLOBAL_PROPERTIES_HOME;
import static com.ericsson.component.aia.services.exteps.io.adapter.common.Constants.GLOBAL_PROPERTIES_TEST_FILE;
import static com.ericsson.component.aia.services.exteps.io.adapter.common.Constants.GLOBAL_NON_DEFAULT_PROPERTIES_TEST_FILE;
import static com.ericsson.component.aia.services.exteps.io.adapter.ipl.TestUtils.createTestConfigurationWithGlobalBrokerPropertiesFile;
import static com.ericsson.component.aia.services.exteps.io.adapter.ipl.TestUtils.createTestConfigurationWithoutGlobalBrokerPropertiesFile;
import static org.junit.Assert.assertEquals;

public class GlobalPropertiesRetrieverUtilTest<T> {

    private AvroOutputAdapter outputAdapter;
    private GenericInputAdapter<T> genericInputAdapter;
    private BatchInputAdapter batchInputAdapter;

    private void avroOutputAdaprteSetup(){
        System.setProperty(GLOBAL_PROPERTIES_HOME, GLOBAL_PROPERTIES_TEST_FILE);
        EventHandlerContext stubbedEventHandlerContext = new StubbedContext(this.outputAdapter.getConfig());
        Whitebox.setInternalState(this.outputAdapter, "eventHandlerContext", stubbedEventHandlerContext);
        this.outputAdapter.doInit();
    }

    private void genericInputAdapterSetup(){
        EventHandlerContext stubbedEventHandlerContext = new StubbedContext(new EpsAdaptiveConfiguration());
        Whitebox.setInternalState(this.genericInputAdapter, "eventHandlerContext", stubbedEventHandlerContext);
        System.setProperty(GLOBAL_PROPERTIES_HOME, GLOBAL_PROPERTIES_TEST_FILE);
        this.genericInputAdapter.doInit();
    }

    private void batchInputAdapterSetup(){
        EventHandlerContext stubbedEventHandlerContext = new StubbedContext(new EpsAdaptiveConfiguration());
        Whitebox.setInternalState(this.batchInputAdapter, "eventHandlerContext", stubbedEventHandlerContext);
        System.setProperty(GLOBAL_PROPERTIES_HOME, GLOBAL_PROPERTIES_TEST_FILE);
        this.batchInputAdapter.doInit();
    }


    @Test
    public void avroOutputAdapter_getDefaultGlobalPropertyFilePath(){
        outputAdapter = new AvroOutputAdapterDefaultGlobalProperties();
        avroOutputAdaprteSetup();
        assertEquals(GlobalPropertiesRetrieverUtil.getFilePath(this.outputAdapter.getConfig()), GLOBAL_PROPERTIES_TEST_FILE);
    }

    @Test
    public void avroOutputAdapter_getGlobalPropertyFilePath(){
        outputAdapter = new AvroOutputAdapterValidCustomConfig();
        avroOutputAdaprteSetup();
        assertEquals(GlobalPropertiesRetrieverUtil.getFilePath(this.outputAdapter.getConfig()), GLOBAL_NON_DEFAULT_PROPERTIES_TEST_FILE);
    }


    @Test
    public void genericInputAdapter_getDefaultGlobalPropertyFilePath(){
        genericInputAdapter = new GenericInputAdapterDefaultGlobalProperties();
        genericInputAdapterSetup();
        assertEquals(GlobalPropertiesRetrieverUtil.getFilePath(this.genericInputAdapter.getConfig()), GLOBAL_PROPERTIES_TEST_FILE);
    }

    @Test
    public void genericInputAdapter_getGlobalPropertyFilePath(){
        genericInputAdapter = new GenericInputAdapterValidCustomConfig();
        genericInputAdapterSetup();
        assertEquals(GlobalPropertiesRetrieverUtil.getFilePath(this.genericInputAdapter.getConfig()), GLOBAL_NON_DEFAULT_PROPERTIES_TEST_FILE);
    }

    @Test
    public void batchInputAdapter_getDefaultGlobalPropertyFilePath(){
        batchInputAdapter = new BatchInputAdapterDefaultGlobalProperties();
        batchInputAdapterSetup();
        assertEquals(GlobalPropertiesRetrieverUtil.getFilePath(this.batchInputAdapter.getConfig()), GLOBAL_PROPERTIES_TEST_FILE);
    }

    @Test
    public void batchInputAdapter_getGlobalPropertyFilePath(){
        batchInputAdapter = new BatchInputAdapterValidCustomConfig();
        batchInputAdapterSetup();
        assertEquals(GlobalPropertiesRetrieverUtil.getFilePath(this.batchInputAdapter.getConfig()), GLOBAL_NON_DEFAULT_PROPERTIES_TEST_FILE);
    }
}

class AvroOutputAdapterDefaultGlobalProperties extends AvroOutputAdapter {
    @Override
    public Configuration getConfig() {
        return createTestConfigurationWithoutGlobalBrokerPropertiesFile("AVRO_PUBLISHER_INTEGRATION_POINT");
    }
}

class AvroOutputAdapterValidCustomConfig extends AvroOutputAdapter {
    @Override
    public Configuration getConfig() {
        return createTestConfigurationWithGlobalBrokerPropertiesFile("AVRO_PUBLISHER_INTEGRATION_POINT");
    }
}

class GenericInputAdapterDefaultGlobalProperties extends  GenericInputAdapter{
    @Override
    public Configuration getConfig() {
        return createTestConfigurationWithoutGlobalBrokerPropertiesFile("RAW_SUBSCRIBER_INTEGRATION_POINT");
    }
}

class GenericInputAdapterValidCustomConfig extends GenericInputAdapter {
    @Override
    public Configuration getConfig() {
        return createTestConfigurationWithGlobalBrokerPropertiesFile("RAW_SUBSCRIBER_INTEGRATION_POINT");
    }
}

class BatchInputAdapterDefaultGlobalProperties extends  BatchInputAdapter{
    @Override
    public Configuration getConfig() {
        return createTestConfigurationWithoutGlobalBrokerPropertiesFile("RAW_SUBSCRIBER_INTEGRATION_POINT");
    }
}

class BatchInputAdapterValidCustomConfig extends BatchInputAdapter {
    @Override
    public Configuration getConfig() {
        return createTestConfigurationWithGlobalBrokerPropertiesFile("RAW_SUBSCRIBER_INTEGRATION_POINT");
    }
}