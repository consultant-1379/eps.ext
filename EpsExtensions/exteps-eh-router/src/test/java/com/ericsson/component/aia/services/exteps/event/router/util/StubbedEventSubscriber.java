package com.ericsson.component.aia.services.exteps.event.router.util;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.component.aia.itpf.common.event.handler.EventSubscriber;

public class StubbedEventSubscriber implements EventSubscriber {

    private String eventSubscriberName;
    private final List<Object> eventList;

    public StubbedEventSubscriber() {
        eventList = new ArrayList<Object>();
    }

    public StubbedEventSubscriber(final String eventSubscriberName) {
        eventList = new ArrayList<Object>();
        this.eventSubscriberName = eventSubscriberName;
    }

    @Override
    public String getIdentifier() {
        return eventSubscriberName;
    }

    @Override
    public void sendEvent(final Object event) {
        eventList.add(event);
    }

    public List<Object> getEvents() {
        return eventList;
    }

}
