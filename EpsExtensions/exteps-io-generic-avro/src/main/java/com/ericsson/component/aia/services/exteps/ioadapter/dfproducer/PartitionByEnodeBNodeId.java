/**
 *
 * (C) Copyright LM Ericsson System Expertise AT/LMI, 2016
 *
 * The copyright to the computer program(s) herein is the property of Ericsson  System Expertise EEI, Sweden.
 * The program(s) may be used and/or copied only with the written permission from Ericsson System Expertise
 * AT/LMI or in  * accordance with the terms and conditions stipulated in the agreement/contract under which
 * the program(s) have been supplied.
 *
 */
package com.ericsson.component.aia.services.exteps.ioadapter.dfproducer;

import java.util.List;
import java.util.Map;

import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;

/**
 * Default partitioner class added in order to provide default partition strategy.
 */
public class PartitionByEnodeBNodeId implements Partitioner {

    @Override
    public void configure(final Map<String, ?> configs) {

    }

    /**
     * Currently partition key is hard coded to <b>GLOBAL_CELL_ID</b> which needs to be taken from configuration.
     */
    @Override
    public int partition(final String topic, final Object key, final byte[] keyBytes, final Object value, final byte[] valueBytes,
                         final Cluster cluster) {
        final GenericRecord record = (GenericRecord) value;
        final Long glId = (Long) record.get("GLOBAL_CELL_ID");
        final List<PartitionInfo> availablePartitionsForTopic = cluster.availablePartitionsForTopic(topic);
        final int size = availablePartitionsForTopic != null ? availablePartitionsForTopic.size() : 0;
        return (int) (((glId == null) || (glId <= 0)) ? 0 : glId % size);
    }

    @Override
    public void close() {
    }

}
