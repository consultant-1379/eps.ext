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
package com.ericsson.component.aia.services.exteps.io.adapter.ipl;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.PartitionInfo;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ericsson.component.aia.services.exteps.io.adapter.partition.AbstractPartitioner;
import com.ericsson.component.aia.services.exteps.io.adapter.partition.ENodeBIdPartitioner;
import com.ericsson.component.aia.services.exteps.io.adapter.partition.GlobalCellIdPartitioner;

public class PartitionerTest {

    private static AbstractPartitioner eNodeBPartitioner = new ENodeBIdPartitioner();
    private static AbstractPartitioner globalCellIdPartitioner = new GlobalCellIdPartitioner();
    private static GenericRecord validGenericRecord;
    private static GenericRecord genericRecordWithNullGlobalCellId;
    private static Cluster kafkaCluster;

    private static Node[] nodeArray = null;
    private static final Set<Node> nodeSet = new HashSet<>();
    private static final Set<PartitionInfo> partitionInfoSet = new HashSet<>();
    private static final Set<String> unauthorizedTopicsSet = new HashSet<>();
    private static final Set<String> internalTopics = new HashSet<>();

    private static final String TOPIC_NAME = "testTopicName";
    private static final String INVALID_TOPIC_NAME = "invalid";
    private static final String TEST_AVRO_SCHEMA = "{ \"type\" : \"record\",\"name\" : \"TEST_EVENT\",\"namespace\" : \"celltrace.s.aa11\","
            + "\"fields\" : [ { \"name\" : \"_NE\", \"type\" : \"string\" },{ \"name\" : \"GLOBAL_CELL_ID\", \"type\" : \"int\" }]}";

    @BeforeClass
    public static void setUp() {
        // Generic Records
        final Schema avroSchema = new Schema.Parser().parse(TEST_AVRO_SCHEMA);
        validGenericRecord = new GenericData.Record(avroSchema);
        validGenericRecord.put("GLOBAL_CELL_ID", (long) 13056527);
        genericRecordWithNullGlobalCellId = new GenericData.Record(avroSchema);
        genericRecordWithNullGlobalCellId.put("GLOBAL_CELL_ID", null);

        // Nodes
        final Node kafkaNode = new Node(2345, "host", 10114);
        nodeArray = new Node[] { kafkaNode };
        nodeSet.add(kafkaNode);

        // Partitions
        final PartitionInfo testPartitionInfo = new PartitionInfo(TOPIC_NAME, 1, kafkaNode, nodeArray, nodeArray);
        final PartitionInfo testPartitionInfo2 = new PartitionInfo(TOPIC_NAME, 1, kafkaNode, nodeArray, nodeArray);
        final PartitionInfo testPartitionInfo3 = new PartitionInfo(TOPIC_NAME, 1, kafkaNode, nodeArray, nodeArray);
        final PartitionInfo testPartitionInfo4 = new PartitionInfo(TOPIC_NAME, 1, kafkaNode, nodeArray, nodeArray);
        partitionInfoSet.add(testPartitionInfo);
        partitionInfoSet.add(testPartitionInfo2);
        partitionInfoSet.add(testPartitionInfo3);
        partitionInfoSet.add(testPartitionInfo4);

        unauthorizedTopicsSet.add("testUnauthorizedTopics");
        kafkaCluster = new Cluster("clusterId", nodeSet, partitionInfoSet, unauthorizedTopicsSet, internalTopics);
    }

    @Test
    public void testENodeBPartitionerWithValidTopicNameAndValidGlobalCellId() {
        final int partitionModules = eNodeBPartitioner.partition(TOPIC_NAME, null, null, validGenericRecord, null, kafkaCluster);
        assertEquals(2, partitionModules);
    }

    @Test
    public void testENodeBPartitionerWithInvalidTopicNameAndValidGlobalCellId() {
        final int partitionModules = eNodeBPartitioner.partition(INVALID_TOPIC_NAME, null, null, validGenericRecord, null, kafkaCluster);
        assertEquals(0, partitionModules);
    }

    @Test
    public void testENodeBPartitionerWithValidTopicNameAndNullGlobalCellId() {
        final int partitionModules = eNodeBPartitioner.partition(TOPIC_NAME, null, null, genericRecordWithNullGlobalCellId, null, kafkaCluster);
        assertEquals(1, partitionModules);
    }

    @Test
    public void testENodeBPartitionerWithInvalidTopicNameAndNullGlobalCellId() {
        final int partitionModules = eNodeBPartitioner.partition(INVALID_TOPIC_NAME, null, null, genericRecordWithNullGlobalCellId, null,
                kafkaCluster);
        assertEquals(0, partitionModules);
    }
    
    @Test
    public void testGlobalCellIdPartitionerWithValidTopicNameAndValidGlobalCellId() {
        final int partitionModules = globalCellIdPartitioner.partition(TOPIC_NAME, null, null, validGenericRecord, null, kafkaCluster);
        assertEquals(3, partitionModules);
    }

    @Test
    public void testGlobalCellIdPartitionerWithInvalidTopicNameAndValidGlobalCellId() {
        final int partitionModules = globalCellIdPartitioner.partition(INVALID_TOPIC_NAME, null, null, validGenericRecord, null, kafkaCluster);
        assertEquals(0, partitionModules);
    }

    @Test
    public void testGlobalCellIdPartitionerWithValidTopicNameAndNullGlobalCellId() {
        final int partitionModules = globalCellIdPartitioner.partition(TOPIC_NAME, null, null, genericRecordWithNullGlobalCellId, null, kafkaCluster);
        assertEquals(1, partitionModules);
    }

    @Test
    public void testGlobalCellIdPartitionerWithInvalidTopicNameAndNullGlobalCellId() {
        final int partitionModules = globalCellIdPartitioner.partition(INVALID_TOPIC_NAME, null, null, genericRecordWithNullGlobalCellId, null, kafkaCluster);
        assertEquals(0, partitionModules);
    }

}
