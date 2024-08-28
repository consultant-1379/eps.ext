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
package com.ericsson.component.aia.services.exteps.ioadapter.builder;

/**
 * @author eachsaj
 * May 3, 2016
 */
public interface IO_CONSTANTS {

    /**
     *  byte [] type raw data
     */
    static final  String FORMAT_RAW = "tcp";
    /**
     * String of words as a record
     */
    static final   String FORMAT_STRING = "string";
    /**
     * Avro record type
     */
    static final   String FORMAT_AVRO = "avro";
    /**
     * EPS data format type supported types are FORMAT_RAW, FORMAT_STRING and FORMAT_AVRO
     */
    static final  String EPS_FORMAT_DATA = "eps.format.data";
    /**
     * input thread count for the consumers
     */
    static final  String INPUT_THREAD_POOL_SIZE_PARAM = "eps.input.thread.pool.size";
    /**
     * output threads for producers
     */
    static final  String OUTPUT_THREAD_POOL_SIZE_PARAM = "eps.output.thread.pool.size";

    /**
     * kafka deserializer cass
     */
    static final  String KAFKA_KEY_DESERIALIZER_CLASS = "key.deserializer";
    /**
     * Kafka Serializer class
     */
    static final  String KAFKA_SERIALIZER_CLASS = "kafka.serializer.class";
    /**
     * Kafka Serializer class
     */
    static final  String KAFKA_VALUE_DE_SERIALIZER_CLASS = "value.deserializer";
    /**
     * eps topic name
     */
    static final  String KAFKA_TOPIC_NAME = "eps.kafka.topicName";
    /**
     *
     */
    String EPS_OUTPUT_KAFKA_PARTITION_KEY = "eps.output.kafka.partition.key";
    /**
     *
     */
    String EPS_OUTPUT_KAFKA_PARTITION_KEY_TYPE = "eps.output.kafka.partition.key.type";
    /**
     *
     */
    String VALUE_SERIALIZER = "value.serializer";
    /**
     *
     */
    String KEY_SERIALIZER = "key.serializer";



}
