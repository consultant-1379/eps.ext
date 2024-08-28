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

package com.ericsson.component.aia.services.exteps.io.adapter.partition;

import java.util.HashMap;
import java.util.Map;

import org.apache.avro.generic.GenericRecord;

import com.ericsson.component.aia.itpf.common.config.Configuration;
import com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler;
import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;

/**
 * PartitionAssigner and events per node metrics
 * Should only be used in conjunction with AvroOutputPartitionerAdapter.java
 */
public class PartitionAssigner extends AbstractEventHandler implements EventInputHandler {
    private Map<Integer, Integer> enodeBIdsToPartition = new HashMap<>();

    private EventStatisticsPerNode eventStatisticsPerNode;

    @Override
    protected void doInit() {
        eventStatisticsPerNode = new EventStatisticsPerNode();
        EsiEventMetricHandler.setUpEventPerNodeMetrics(eventStatisticsPerNode);
        final String filepath = getConfig().getStringProperty("csv.filepath");
        enodeBIdsToPartition = CsvConfigurationFileReader.getCsvContent(filepath);
    }

    @Override
    public void onEvent(final Object inputEvent) {
        if (inputEvent == null) {
            throw new IllegalArgumentException("Input event must not be null");
        } else if (inputEvent instanceof GenericRecord) {
            final GenericRecord record = (GenericRecord) inputEvent;
            final Object globalCellId = record.get("GLOBAL_CELL_ID");
            Integer enodeBId = 1;
            if (globalCellId != null) {
                enodeBId = (int) getENodeBId((long) globalCellId);
            }
            eventStatisticsPerNode.setEventCountPerId(enodeBId.longValue());
            final Integer partitionNumber = enodeBIdsToPartition.get(enodeBId);
            final GenericRecordWithPartitionNumber genericRecordWithPartitionNumber = new GenericRecordWithPartitionNumber(record, partitionNumber);
            sendToAllSubscribers(genericRecordWithPartitionNumber);
        } else {
            throw new IllegalArgumentException("Input event must be a type of GenericRecord");
        }
    }

    private static long getENodeBId(final long globalCellId) {
        return globalCellId >> 8 & 0x000fffff;
    }

    protected Configuration getConfig() {
        return getConfiguration();
    }

    protected int getMapSize() {
        return enodeBIdsToPartition.size();
    }

}
