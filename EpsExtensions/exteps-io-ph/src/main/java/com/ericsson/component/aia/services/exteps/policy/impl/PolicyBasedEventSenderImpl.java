package com.ericsson.component.aia.services.exteps.policy.impl;

import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import com.ericsson.component.aia.services.exteps.policy.PolicyBasedEventSender;

public class PolicyBasedEventSenderImpl<K, V> implements PolicyBasedEventSender<K, V> {

    private final String policyName;

    public PolicyBasedEventSenderImpl(final String policyName) {
        this.policyName = policyName;
    }

    @Override
    public Future<RecordMetadata> sendEvent(ProducerEvent<K, V> event) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RecordMetadata> sendRecord(ProducerRecord<K, V> record) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RecordMetadata> sendEvent(ProducerEvent<K, V> event, Callback callback) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RecordMetadata> sendRecord(ProducerRecord<K, V> record, Callback callback) {
        // TODO Auto-generated method stub
        return null;
    }

}
