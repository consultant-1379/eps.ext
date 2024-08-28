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

import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;

/**
 * Abstract Kafka partitioner class used for assigning a partition number based on the value of an Id used from the event. Currently only supported
 * Avro GenericRecord input as the message value type.
 *
 */
public abstract class AbstractPartitioner implements Partitioner {

    protected static final String GLOBAL_CELL_ID = "GLOBAL_CELL_ID";
    protected static final int DEFAULT_PARTITION_COUNT = 1;

    @Override
    public int partition(final String topic, final Object key, final byte[] keyBytes, final Object value, final byte[] valueBytes,
                         final Cluster cluster) {
        final long idAttribute = getAnIdAttributeFromEvent(value);
        final int partitionCount = getPartitionCount(topic, cluster);
        return toPositive((int) (idAttribute % partitionCount));
    }

    /**
     * Method to return the number of partitions in a cluster for a given topic.
     * @param topic The kafka topic. Method will count the number of partitions in this topic.
     * @param cluster Kafka cluster containing partition info.
     * @return number of partitions in the given topic.
     */
    protected int getPartitionCount(final String topic, final Cluster cluster) {
        final List<PartitionInfo> availablePartitionsForTopic = cluster.availablePartitionsForTopic(topic);
        return (availablePartitionsForTopic == null || availablePartitionsForTopic.isEmpty())
                ? DEFAULT_PARTITION_COUNT : availablePartitionsForTopic.size();
    }

    /**
     * Method to get one of the ID attributes from an event. e.g method to get GLOBAL_CELL_ID.
     * @param event
     *            event in generic record format.
     * @return ID
     */
    protected abstract long getAnIdAttributeFromEvent(final Object event);

    @Override
    public void configure(final Map<String, ?> configs) {
        //No implementation
    }

    @Override
    public void close() {
        //No implementation
    }

    /**
     * Method to convert a number number to positive. If the number is already positive it will do nothing.
     * If the number is negative it will convert it to to a positive number with the same value.
     * @param number The number to convert to positive
     * @return positive number.
     */
    protected static int toPositive(final int number) {
        return number & 0x7fffffff;
    }

}
