package com.ericsson.component.aia.services.exteps.policy.model;

import java.util.List;
import java.util.Properties;

public abstract class Policy {

    private String policyName;
    private List<Topic> topicList;
    private Properties properties;

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public List<Topic> getTopicList() {
        return topicList;
    }

    public void setTopicList(List<Topic> topicList) {
        this.topicList = topicList;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }



}
