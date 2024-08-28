package com.ericsson.component.aia.services.exteps.policy;

import org.apache.kafka.clients.producer.ProducerRecord;

import com.ericsson.component.aia.services.exteps.policy.impl.ProducerEvent;

public interface EventListener<K, V> {

    void onEvent(ProducerEvent<K, V> event);

    void onEvent(ProducerRecord<K, V> event);
}
