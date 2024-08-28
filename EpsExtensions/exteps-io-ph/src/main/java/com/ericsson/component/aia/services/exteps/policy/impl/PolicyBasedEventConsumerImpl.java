package com.ericsson.component.aia.services.exteps.policy.impl;

import com.ericsson.component.aia.services.exteps.policy.EventListener;
import com.ericsson.component.aia.services.exteps.policy.PolicyBasedEventConsumer;

public class PolicyBasedEventConsumerImpl<K, V> implements PolicyBasedEventConsumer<K, V> {

    private final String policyName;

    public PolicyBasedEventConsumerImpl(final String policyName){
        this.policyName = policyName;
    }

    @Override
    public void registerEventListener(EventListener<K, V> eventListener) {
        // TODO Auto-generated method stub

    }
}
