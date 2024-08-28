/*
 *
 * (C) Copyright LM Ericsson System Expertise AT/LMI, 2016
 *
 * The copyright to the computer program(s) herein is the property of Ericsson  System Expertise EEI, Sweden.
 * The program(s) may be used and/or copied only with the written permission from Ericsson System Expertise
 * AT/LMI or in  * accordance with the terms and conditions stipulated in the agreement/contract under which
 * the program(s) have been supplied.
 *
 */
package com.ericsson.component.aia.services.exteps.io.adapter.partition;

import org.apache.avro.generic.GenericRecord;

/**
 * Kafka partitioner class used for assigning a partition number based on the value of the Global Cell Id of the event. Currently only supported Avro
 * GenericRecord input as the message value type.
 *
 */
public class GlobalCellIdPartitioner extends AbstractPartitioner {

    private static final int DEFAULT_GLOBAL_CELL_ID = 1;

    /**
     * Method to return the Global Cell ID from an event. If the event does not contain a GLOBAL_CELL_ID field, method will return a default
     * GLOBAL_CELL_ID value.
     * @param value
     *            event in generic record format.
     * @return Global Cell ID
     */
    @Override
    protected long getAnIdAttributeFromEvent(final Object event) {
        final GenericRecord record = (GenericRecord) event;
        final Object globalCellId = record.get(GLOBAL_CELL_ID);
        return globalCellId != null ? (long) globalCellId : DEFAULT_GLOBAL_CELL_ID;
    }
}