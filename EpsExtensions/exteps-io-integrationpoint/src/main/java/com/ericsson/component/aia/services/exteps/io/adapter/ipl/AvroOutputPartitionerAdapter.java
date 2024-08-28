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

import org.apache.avro.generic.GenericRecord;

import com.ericsson.component.aia.services.exteps.io.adapter.partition.GenericRecordWithPartitionNumber;

/**
 * The Class AvroOutputPartitionerAdapter extends AvroOutputAdapter, responsible
 * for publishing Avro GenericRecords using IPL to a particular partition of a
 * topic if the partition number is null, the kafka partitioner class will be
 * used.
 */
public class AvroOutputPartitionerAdapter extends AvroOutputAdapter {

    public static final String AVRO_PARTITIONER_URI = "avro_partitioner:/";
    private static final String EVENT_ID_FIELD = "_ID";

    @Override
    public void onEvent(final Object inputEvent) {
        if (inputEvent == null) {
            throw new IllegalArgumentException("Input event must not be null");
        } else if (inputEvent instanceof GenericRecordWithPartitionNumber) {
            final GenericRecordWithPartitionNumber genericRecordWithPartitionNumber = (GenericRecordWithPartitionNumber) inputEvent;
            final GenericRecord record = genericRecordWithPartitionNumber.getGenericRecord();
            sendRecord(record, genericRecordWithPartitionNumber.getPartitionNumber());
            if (getEpsExtStatisticsHelper().isStatisticsOn()) {
                getEpsExtStatisticsHelper().mark();
            }
        } else {
            throw new IllegalArgumentException("Input event must be a type of GenericRecordWithPartitionNumber");
        }
    }

    protected void sendRecord(final GenericRecord record, Integer partitionNumber) {
        getPublisher().sendRecord(String.valueOf(record.get(EVENT_ID_FIELD)), record, partitionNumber);
    }

    @Override
    public boolean understandsURI(final String uri) {
        return uri != null && uri.startsWith(AVRO_PARTITIONER_URI);
    }
}
