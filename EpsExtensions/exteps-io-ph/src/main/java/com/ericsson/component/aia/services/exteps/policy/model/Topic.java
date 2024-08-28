package com.ericsson.component.aia.services.exteps.policy.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Topic {

    private String name;
    private List<String> events;
    private Properties topicProperties;

    public Topic() {
        events = new ArrayList<String>();
        topicProperties = new Properties();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getEvents() {
        return events;
    }

    public void setEvents(List<String> events) {
        this.events = events;
    }

    public Properties getTopicProperties() {
        return topicProperties;
    }

    public void setTopicProperties(Properties topicProperties) {
        this.topicProperties = topicProperties;
    }


}
