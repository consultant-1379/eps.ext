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
 * Kafka partitioner class used for assigning a partition number based on the value of the ENodeB Id of the event. Currently only supported Avro
 * GenericRecord input as the message value type.
 *
 */
public class ENodeBIdPartitioner extends AbstractPartitioner {

    protected static final int DEFAULT_ENODEB_ID = 1;

    /**
     * Method to return the ENodeB ID from and event. If the event does not contain a ENodeB field, method will return a default ENodeB value.
     * @param value
     *            event in generic record format.
     * @return eNodeB ID
     */
    @Override
    protected long getAnIdAttributeFromEvent(final Object event) {
        final GenericRecord record = (GenericRecord) event;
        final Object globalCellId = record.get(GLOBAL_CELL_ID);
        return globalCellId != null ? getENodeBId((long) globalCellId) : DEFAULT_ENODEB_ID;
    }

    /**
     * The eNB is the value contained in the bits from bit 9 to bit 20 where the least significant bit is 1.
     * @param globalCellId
     *            - EVENT_PARAM_GLOBAL_CELL_ID from the event
     * @return eNBId
     */
    private static long getENodeBId(final long globalCellId) {
        return globalCellId >> 8 & 0x000fffff;
    }
}