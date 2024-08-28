package com.ericsson.component.aia.services.exteps.policy.impl;

public class ProducerEvent<K, V> {

    private K key;
    private V value;
    private String eventName;


    public ProducerEvent(String eventName) {
        this.eventName = eventName;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public String getEventName() {
        return eventName;
    }

}
