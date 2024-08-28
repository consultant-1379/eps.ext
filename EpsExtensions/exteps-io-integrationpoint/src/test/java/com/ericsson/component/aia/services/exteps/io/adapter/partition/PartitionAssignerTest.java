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

package com.ericsson.component.aia.services.exteps.io.adapter.partition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import com.ericsson.component.aia.itpf.common.config.Configuration;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.component.aia.itpf.common.event.handler.EventSubscriber;
import com.ericsson.component.aia.services.eps.coordination.EpsAdaptiveConfiguration;
import com.ericsson.component.aia.services.exteps.io.adapter.ipl.StubbedContext;

public class PartitionAssignerTest {
    private boolean isSubscriberSendEventMethodCalled;
    private boolean isInstanceOfGenericRecordWithPartitionNumber;

    private PartitionAssigner partitionAssigner;

    private static String SYSTEM_PROPERTY_NAME = "s";

    private static int expectedSize = 10;

    @Before
    public void setUp() {
        isSubscriberSendEventMethodCalled = false;
        isInstanceOfGenericRecordWithPartitionNumber = false;
        partitionAssigner = new StubbedPartitionAssigner();
        final EventHandlerContext stubbedEventHandlerContext = new StubbedContext(partitionAssigner.getConfig());
        Whitebox.setInternalState(partitionAssigner, "eventHandlerContext", stubbedEventHandlerContext);
        Whitebox.setInternalState(stubbedEventHandlerContext, "eventSubscribers", createMockedEventSubscribers());
        partitionAssigner.doInit();
    }

    @After
    public void destroy() {
        partitionAssigner.destroy();
    }

    @Test
    public void testDoInit_parsesCSVCorrectly() {
        System.setProperty(SYSTEM_PROPERTY_NAME, "testDoInit_parsesCSVCorrectly");
        assertEquals(expectedSize, partitionAssigner.getMapSize());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOnEvent_nullEvent() {
        System.setProperty(SYSTEM_PROPERTY_NAME, "testOnEvent_nullEvent");
        partitionAssigner.onEvent(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOnEvent_wrongEventType() {
        System.setProperty(SYSTEM_PROPERTY_NAME, "testOnEvent_wrongEventType");
        partitionAssigner.onEvent(new byte[0]);
    }

    @Test
    public void testOnEvent_correctEvent() throws IOException {
        System.setProperty(SYSTEM_PROPERTY_NAME, "testOnEvent_correctEvent");
        final GenericRecord genericRecord = createGenericRecord();
        partitionAssigner.onEvent(genericRecord);
        assertTrue(isSubscriberSendEventMethodCalled);
        assertTrue(isInstanceOfGenericRecordWithPartitionNumber);
    }

    private GenericRecord createGenericRecord() throws IOException {
        final File schemaFile = new File("src/test/resources/INTERNAL_EVENT_ADMISSION_BLOCKING_STARTED.avsc");
        final Schema schema = new Schema.Parser().parse(schemaFile);
        final GenericRecord record = new GenericData.Record(schema);
        record.put("GLOBAL_CELL_ID", 765952L);
        return record;
    }

    private class EventSubscriberForTest implements EventSubscriber {

        @Override
        public String getIdentifier() {
            return null;
        }

        @Override
        public void sendEvent(final Object event) {
            isSubscriberSendEventMethodCalled = true;
            isInstanceOfGenericRecordWithPartitionNumber = event instanceof GenericRecordWithPartitionNumber ? true : false;
        }
    }

    private class StubbedPartitionAssigner extends PartitionAssigner {

        private final Map<String, Object> configMap = new HashMap<>();

        @Override
        protected Configuration getConfig() {
            final EpsAdaptiveConfiguration config = new EpsAdaptiveConfiguration();
            configMap.put("csv.filepath", "src/test/resources/enodeBIds.csv");
            config.setConfiguration(configMap);
            return config;
        }
    }

    private Collection<EventSubscriber> createMockedEventSubscribers() {
        final EventSubscriber es = new EventSubscriberForTest();
        final Collection<EventSubscriber> subscribers = new ArrayList<>();
        subscribers.add(es);
        return subscribers;
    }
}
