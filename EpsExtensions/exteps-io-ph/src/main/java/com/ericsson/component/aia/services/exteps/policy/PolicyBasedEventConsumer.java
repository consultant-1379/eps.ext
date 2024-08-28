package com.ericsson.component.aia.services.exteps.policy;

public interface PolicyBasedEventConsumer<K, V> {

    public void registerEventListener(EventListener<K, V> eventListener);

}