package com.ericsson.component.aia.services.exteps.policy;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import com.ericsson.component.aia.services.exteps.policy.impl.ProducerEvent;

public interface PolicyBasedEventSender<K, V> {

    java.util.concurrent.Future<RecordMetadata> sendEvent(ProducerEvent<K, V> event);

    java.util.concurrent.Future<RecordMetadata> sendRecord(ProducerRecord<K, V> record);

    java.util.concurrent.Future<RecordMetadata> sendEvent(ProducerEvent<K, V> event, Callback callback);

    java.util.concurrent.Future<RecordMetadata> sendRecord(ProducerRecord<K, V> record, Callback callback);

}
