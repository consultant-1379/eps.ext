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

import java.util.Properties;



import com.ericsson.component.aia.common.transport.config.KafkaPublisherConfiguration;
import com.ericsson.component.aia.common.transport.config.KafkaSubscriberConfiguration;
import com.ericsson.component.aia.common.transport.config.builders.PublisherConfigurationBuilder;
import com.ericsson.component.aia.common.transport.config.builders.SubscriberConfigurationBuilder;
import com.ericsson.component.aia.common.transport.service.MessageServiceTypes;
import com.ericsson.component.aia.common.transport.service.Publisher;
import com.ericsson.component.aia.common.transport.service.Subscriber;
import com.ericsson.component.aia.common.transport.service.kafka.KafkaFactory;
import com.ericsson.component.aia.common.transport.service.kafka.subscriber.KafkaConsumerFactory;
import com.ericsson.component.aia.services.exteps.ioadapter.dfconsumer.EPSMessageListener;

import static com.google.common.base.Preconditions.checkArgument;



/**
 * A factory to build and configure the publisher and consumers
 * @author eachsaj
 * March 25, 2016
 */
public class ServiceFactory {
    private static int threadCount;
    /**
     * A method to create subscriber
     * @param uRI destination type uri
     * @param allProperties connection properties
     */
    public static <K, V>  Subscriber<K, V> createSubscriber(MessageServiceTypes uRI, Properties allProperties, EPSMessageListener<V> instance) {
        checkArgument(uRI != null, "URI cannot be null.");
        checkArgument(allProperties != null, "Properties  cannot be null.");
        checkArgument(instance != null, "Listener object   cannot be null.");
        threadCount = Integer.parseInt(allProperties.getProperty(IO_CONSTANTS.INPUT_THREAD_POOL_SIZE_PARAM, "1"));
        switch (uRI) {
            // kafka connections
            case KAFKA: return createKafkaSubscriber(allProperties, instance);
            // Zero mq connection
            case ZMQ: createZMQSubscriber(allProperties, instance);
            // ActiveMQ connection
            case AMQ: createAMQSubscriber(allProperties, instance);

        }
        throw new UnsupportedOperationException("Unknown type Subscriber requested.");


    }

    /**
     * A method to create Publisher
     * @param uRI destination type uri
     * @param allProperties connection properties
     */
    public static <K, V>  Publisher<K, V> createPublisher(MessageServiceTypes uRI, Properties allProperties) {
        checkArgument(uRI != null, "URI cannot be null.");
        checkArgument(allProperties != null, "Properties  cannot be null.");
        threadCount = Integer.parseInt(allProperties.getProperty(IO_CONSTANTS.INPUT_THREAD_POOL_SIZE_PARAM, "1"));
        switch (uRI) {
            // kafka connections
            case KAFKA: return createKafProducer(allProperties);
            // Zero mq connection
            case ZMQ: throw new UnsupportedOperationException("Not implemented yet");
            // ActiveMQ connection
            case AMQ: throw new UnsupportedOperationException("Not implemented yet");

        }
        throw new UnsupportedOperationException("Unknown type Subscriber requested.");


    }


    /**
     * @param allProperties
     * @return
     */
    private static <K, V>  Publisher<K, V> createKafProducer(Properties props) {
        final KafkaPublisherConfiguration<V> build = PublisherConfigurationBuilder.<K, V>createkafkaPublisherBuilder(props)
            .addKeySerializer(props.getProperty(IO_CONSTANTS.KEY_SERIALIZER))
            .addValueSerializer(props.getProperty(IO_CONSTANTS.VALUE_SERIALIZER))
            .addProcessors(Integer.parseInt(props.getProperty(IO_CONSTANTS.OUTPUT_THREAD_POOL_SIZE_PARAM))).build();
        final Publisher<K, V> createKafkaPublisher = KafkaFactory.<K, V>createKafkaPublisher(build);
        //createKafkaPublisher.init(build);
        return  createKafkaPublisher;
    }

    /**
     * AMQ subscriber creation method
     * @param allProperties is the connection properties
     */
    private static <K, V>   Subscriber<K, V>  createAMQSubscriber(Properties allProperties, EPSMessageListener<V> instance) {
        throw new UnsupportedOperationException("Not implemented yet");
      // not implemented

    }

    /**
     * ZMQ implementation
     * @param allProperties
     */
    private static <K, V>   Subscriber<K, V>  createZMQSubscriber(Properties allProperties, EPSMessageListener<V> instance) {
          // not implemented
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * A method to create and configure the Kafka Subscriber
     * @param allProperties
     */
    private static <K, V>   Subscriber<K, V>  createKafkaSubscriber(Properties props, EPSMessageListener<V> instance) {
        final String topicNames = props.getProperty(IO_CONSTANTS.KAFKA_TOPIC_NAME);
        final String kafkkeySerializer = props.getProperty(IO_CONSTANTS.KAFKA_KEY_DESERIALIZER_CLASS);
        final String kafkaValueDeSerializer = props.getProperty(IO_CONSTANTS.KAFKA_VALUE_DE_SERIALIZER_CLASS);
        final KafkaSubscriberConfiguration<V>  conf =  SubscriberConfigurationBuilder.<K, V>createkafkaConsumerBuilder(props)
             .addValueDeserializer(kafkaValueDeSerializer)
             .addKeyDeserializer(kafkkeySerializer)
             .addTopic(topicNames)
             .addProcessors(threadCount)
             .enableManaged()
             .addListener(instance)
             .build();
        Subscriber<K, V> consumer = KafkaConsumerFactory.create(conf);
        return consumer;
    }

}
