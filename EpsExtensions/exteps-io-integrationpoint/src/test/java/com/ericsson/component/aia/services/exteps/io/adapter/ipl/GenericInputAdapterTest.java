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
package com.ericsson.component.aia.services.exteps.io.adapter.ipl;

import static com.ericsson.component.aia.services.exteps.io.adapter.common.Constants.GLOBAL_PROPERTIES_HOME;
import static com.ericsson.component.aia.services.exteps.io.adapter.common.Constants.GLOBAL_PROPERTIES_TEST_FILE;
import static com.ericsson.component.aia.services.exteps.io.adapter.ipl.TestUtils.assertbrokerAddressFileLocationannotBeUpdated;
import static com.ericsson.component.aia.services.exteps.io.adapter.ipl.TestUtils.assertValidUri;
import static com.ericsson.component.aia.services.exteps.io.adapter.ipl.TestUtils.assertbrokerAddressFileLocationDefault;
import static com.ericsson.component.aia.services.exteps.io.adapter.ipl.TestUtils.assertbrokerAddressFileLocationCustom;
import static com.ericsson.component.aia.services.exteps.io.adapter.ipl.TestUtils.createTestConfiguration;
import static com.ericsson.component.aia.services.exteps.io.adapter.ipl.TestUtils.createTestConfigurationWithoutGlobalBrokerPropertiesFile;
import static com.ericsson.component.aia.services.exteps.io.adapter.ipl.TestUtils.createTestConfigurationWithGlobalBrokerPropertiesFile;
import static com.ericsson.component.aia.services.exteps.io.adapter.ipl.TestUtils.createTestConfigurationWithInvalidGlobalBrokerPropertiesFilePath;


import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Collection;

import org.apache.avro.generic.GenericRecord;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.powermock.reflect.Whitebox;

import com.ericsson.component.aia.itpf.common.config.Configuration;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.component.aia.services.eps.coordination.EpsAdaptiveConfiguration;

@RunWith(Parameterized.class)
public class GenericInputAdapterTest<T> {

    private final GenericInputAdapter<T> inputAdapter;

    public GenericInputAdapterTest(final GenericInputAdapter<T> inputAdapter){
        this.inputAdapter = inputAdapter;
        EventHandlerContext stubbedEventHandlerContext = new StubbedContext(new EpsAdaptiveConfiguration());
        Whitebox.setInternalState(inputAdapter, "eventHandlerContext", stubbedEventHandlerContext);
    }

    @Parameters
    public static Collection<GenericInputAdapter> genericInputAdapterImplementations() {
        return Arrays.asList(new GenericInputAdapter[] { new StubbedRawInputAdapter(), new StubbedAvroInputAdapter(), new MockedRawInputAdapterDefaultGlobalProperties(), new MockedRawInputAdapterValidCustomConfig(), new MockedRawInputAdapterInvalidCustomConfig()});
    }

    @Before
    public void setUp() {
        System.setProperty(GLOBAL_PROPERTIES_HOME, GLOBAL_PROPERTIES_TEST_FILE);
        inputAdapter.doInit();
    }

    @Test
    public void testSubscriberCreatedSuccessfully() {
        assertNotNull(inputAdapter.getSubscriber());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testOnEvent() {
        inputAdapter.onEvent("");
    }

    @Test
    public void testUnderstandsUri() {
        assertValidUri(inputAdapter, GenericInputAdapter.URI);
    }


    @Test
    public void doInit_validPropertyConfig_defaultGlobalPropertiesFilePath() {
        if(inputAdapter instanceof MockedRawInputAdapterDefaultGlobalProperties)
            assertbrokerAddressFileLocationDefault();
    }

    @Test
    public void doInit_validCustomConfig_customGlobalPropertiesFilePath() {
        if(inputAdapter instanceof MockedRawInputAdapterValidCustomConfig)
            assertbrokerAddressFileLocationCustom();
    }

    @Test
    public void doInit_invalidCustomConfig_customGlobalPropertiesFilePath() {
        if(inputAdapter instanceof MockedRawInputAdapterInvalidCustomConfig)
            assertbrokerAddressFileLocationannotBeUpdated();
    }

}

class StubbedRawInputAdapter extends GenericInputAdapter<byte[]> {
    @Override
    public Configuration getConfig() {
        return createTestConfiguration("RAW_SUBSCRIBER_INTEGRATION_POINT");
    }
}

class StubbedAvroInputAdapter extends GenericInputAdapter<GenericRecord> {
    @Override
    public Configuration getConfig() {
        return createTestConfiguration("AVRO_SUBSCRIBER_INTEGRATION_POINT");
    }
}

class MockedRawInputAdapterDefaultGlobalProperties extends GenericInputAdapter<byte[]> {
    @Override
    public Configuration getConfig() {
        return createTestConfigurationWithoutGlobalBrokerPropertiesFile("RAW_SUBSCRIBER_INTEGRATION_POINT");
    }
}

class MockedRawInputAdapterValidCustomConfig extends GenericInputAdapter<byte[]> {
    @Override
    public Configuration getConfig() {
        return createTestConfigurationWithGlobalBrokerPropertiesFile("RAW_SUBSCRIBER_INTEGRATION_POINT");
    }
}

class MockedRawInputAdapterInvalidCustomConfig extends GenericInputAdapter<byte[]> {
    @Override
    public Configuration getConfig() {
        return createTestConfigurationWithInvalidGlobalBrokerPropertiesFilePath("RAW_SUBSCRIBER_INTEGRATION_POINT");
    }
}
