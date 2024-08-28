/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
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
import static com.ericsson.component.aia.services.exteps.io.adapter.ipl.TestUtils.createTestConfiguration;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import com.ericsson.component.aia.itpf.common.config.Configuration;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.component.aia.services.exteps.io.adapter.partition.GenericRecordWithPartitionNumber;

public class AvroOutputPartitionerAdapterTest {

    private List<GenericRecord> records = new ArrayList<>();

    private AvroOutputAdapter outputAdapter;

    @Before
    public void setUp() {
        System.setProperty(GLOBAL_PROPERTIES_HOME, GLOBAL_PROPERTIES_TEST_FILE);
        outputAdapter = new StubbedAvroOutputPartitionerAdapter();
        EventHandlerContext stubbedEventHandlerContext = new StubbedContext(outputAdapter.getConfig());
        Whitebox.setInternalState(outputAdapter, "eventHandlerContext", stubbedEventHandlerContext);
        outputAdapter.doInit();
    }

    @After
    public void destroy() {
        outputAdapter.destroy();
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
    public void testOnEvent_correctEventTypeGoesToPartitionSpecified() throws IOException {
        final File schemaFile = new File("src/test/resources/INTERNAL_EVENT_ADMISSION_BLOCKING_STARTED.avsc");
        final File schemaFile2 = new File("src/test/resources/RRC_RRC_CONNECTION_SETUP.avsc");
        final Schema schema = new Schema.Parser().parse(schemaFile);
        final Schema schema2 = new Schema.Parser().parse(schemaFile2);
        final GenericRecord record1 = new GenericData.Record(schema);
        final GenericRecord record2 = new GenericData.Record(schema2);

        final GenericRecordWithPartitionNumber newRecord1 = new GenericRecordWithPartitionNumber(record1, 0);
        outputAdapter.onEvent(newRecord1);
        assertEquals(1, records.size());
        assertEquals(record1, records.get(0));

        final GenericRecordWithPartitionNumber newRecord2 = new GenericRecordWithPartitionNumber(record2, 0);
        outputAdapter.onEvent(newRecord2);
        assertEquals(2, records.size());
        assertEquals(record2, records.get(0));
        assertEquals(record1, records.get(1));

    }

    private class StubbedAvroOutputPartitionerAdapter extends AvroOutputPartitionerAdapter {
        @Override
        public Configuration getConfig() {
            return createTestConfiguration("AVRO_PUBLISHER_INTEGRATION_POINT");
        }

        @Override
        public void sendRecord(final GenericRecord record, Integer partitionNumber) {
            records.add(partitionNumber, record);
        }
    }
}
