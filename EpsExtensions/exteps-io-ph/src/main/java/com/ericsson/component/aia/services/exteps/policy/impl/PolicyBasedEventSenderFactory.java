package com.ericsson.component.aia.services.exteps.policy.impl;

import com.ericsson.component.aia.services.exteps.policy.PolicyBasedEventSender;

public class PolicyBasedEventSenderFactory<K, V> {

    private String location;

    public PolicyBasedEventSenderFactory(final String location) {
        this.location = location;
    }
    public PolicyBasedEventSender<K, V> eventSender(String policyName) {
        return new PolicyBasedEventSenderImpl<K, V>(policyName);
    }
}
