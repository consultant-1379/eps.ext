package com.ericsson.component.aia.services.exteps.policy.impl;

import com.ericsson.component.aia.services.exteps.policy.PolicyBasedEventConsumer;

public class PolicyBasedEventConsumerFactory<K, V> {

    private String location;

    public PolicyBasedEventConsumerFactory(final String location) {
        this.location = location;
    }

    public PolicyBasedEventConsumer<K, V> eventConsumer(String policyName) {
        return new PolicyBasedEventConsumerImpl<K, V>(policyName);
    }

}
