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

import static com.ericsson.aia.ipl.model.Constants.KAFKA_BROKERS_ADDRESSES_FILE_PROPERTY_NAME;
import static com.ericsson.component.aia.services.exteps.io.adapter.common.Constants.GLOBAL_PROPERTIES_HOME;
import static com.ericsson.component.aia.services.exteps.io.adapter.common.Constants.GLOBAL_PROPERTIES_TEST_FILE;
import static com.ericsson.component.aia.services.exteps.io.adapter.common.Constants.GLOBAL_NON_DEFAULT_PROPERTIES_TEST_FILE;
import static com.ericsson.component.aia.services.exteps.io.adapter.ipl.TestUtils.createTestConfiguration;
import static com.ericsson.component.aia.services.exteps.io.adapter.ipl.TestUtils.assertValidUri;
import static com.ericsson.component.aia.services.exteps.io.adapter.ipl.TestUtils.createTestConfigurationWithoutGlobalBrokerPropertiesFile;
import static com.ericsson.component.aia.services.exteps.io.adapter.ipl.TestUtils.createTestConfigurationWithGlobalBrokerPropertiesFile;
import static com.ericsson.component.aia.services.exteps.io.adapter.ipl.TestUtils.createTestConfigurationWithInvalidGlobalBrokerPropertiesFilePath;
import static java.lang.System.getProperty;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import com.ericsson.component.aia.itpf.common.config.Configuration;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;

public class AvroOutputAdapterTest {

    private final List<GenericRecord> records = new ArrayList<>();

    private class StubbedAvroOutputAdapter extends AvroOutputAdapter {
        @Override
        public Configuration getConfig() {
            return createTestConfiguration("AVRO_PUBLISHER_INTEGRATION_POINT");
        }

        @Override
        public void sendRecord(final GenericRecord record) {
            records.add(record);
        }
    }

    private AvroOutputAdapter outputAdapter;

    @Before
    public void setUp() {
        System.setProperty(GLOBAL_PROPERTIES_HOME, GLOBAL_PROPERTIES_TEST_FILE);
        outputAdapter = new StubbedAvroOutputAdapter();
        EventHandlerContext stubbedEventHandlerContext = new StubbedContext(outputAdapter.getConfig());
        Whitebox.setInternalState(outputAdapter, "eventHandlerContext", stubbedEventHandlerContext);
        outputAdapter.doInit();
    }

    @After
    public void destroy() {
        outputAdapter.destroy();
    }

    @Test
    public void testPublisherCreatedSuccessfully() {
        assertNotNull(outputAdapter.getPublisher());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOnEvent_nullEvent() {
        outputAdapter.onEvent(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOnEvent_wrongEventType() {
        outputAdapter.onEvent(new byte[0]);
    }

    @Test
    public void testOnEvent_correctEventType() throws IOException {
        final File schemaFile = new File("src/test/resources/INTERNAL_EVENT_ADMISSION_BLOCKING_STARTED.avsc");
        final Schema schema = new Schema.Parser().parse(schemaFile);
        final GenericRecord record = new GenericData.Record(schema);
        outputAdapter.onEvent(record);
        assertEquals(1, records.size());
        assertEquals(record, records.get(0));
    }

    @Test
    public void testUnderstandsUri() {
        assertValidUri(outputAdapter, AvroOutputAdapter.URI);
    }


    private void testBrokerAddressFile(){
        System.setProperty(GLOBAL_PROPERTIES_HOME, GLOBAL_PROPERTIES_TEST_FILE);
        EventHandlerContext stubbedEventHandlerContext = new StubbedContext(this.outputAdapter.getConfig());
        Whitebox.setInternalState(this.outputAdapter, "eventHandlerContext", stubbedEventHandlerContext);
        this.outputAdapter.doInit();
    }

    @Test
    public void doInit_validPropertyConfig_defaultGlobalPropertiesFilePath() {
        outputAdapter = new MockedAvroOutputAdapterDefaultGlobalProperties();
        testBrokerAddressFile();
        assertEquals(getProperty(KAFKA_BROKERS_ADDRESSES_FILE_PROPERTY_NAME), GLOBAL_PROPERTIES_TEST_FILE);
    }

    @Test
    public void doInit_validCustomConfig_customGlobalPropertiesFilePath() {
        outputAdapter = new MockedAvroOutputAdapterValidCustomConfig();
        testBrokerAddressFile();
        assertEquals(getProperty(KAFKA_BROKERS_ADDRESSES_FILE_PROPERTY_NAME), GLOBAL_NON_DEFAULT_PROPERTIES_TEST_FILE);
    }

    @Test
    public void doInit_invalidCustomConfig_customGlobalPropertiesFilePath() {
        outputAdapter = new MockedAvroOutputAdapterInvalidCustomConfig();
        testBrokerAddressFile();
        assertNotEquals(getProperty(KAFKA_BROKERS_ADDRESSES_FILE_PROPERTY_NAME), GLOBAL_PROPERTIES_TEST_FILE);
    }

}



class MockedAvroOutputAdapterDefaultGlobalProperties extends AvroOutputAdapter {
    @Override
    public Configuration getConfig() {
        return createTestConfigurationWithoutGlobalBrokerPropertiesFile("AVRO_PUBLISHER_INTEGRATION_POINT");
    }
}

class MockedAvroOutputAdapterValidCustomConfig extends AvroOutputAdapter {
    @Override
    public Configuration getConfig() {
        return createTestConfigurationWithGlobalBrokerPropertiesFile("AVRO_PUBLISHER_INTEGRATION_POINT");
    }
}

class MockedAvroOutputAdapterInvalidCustomConfig extends AvroOutputAdapter {
    @Override
    public Configuration getConfig() {
        return createTestConfigurationWithInvalidGlobalBrokerPropertiesFilePath("AVRO_PUBLISHER_INTEGRATION_POINT");
    }
}
