package com.ericsson.component.aia.services.exteps.event.router.util;

import java.util.ArrayList;
import java.util.Collection;

import com.ericsson.component.aia.itpf.common.config.Configuration;
import com.ericsson.component.aia.itpf.common.event.ControlEvent;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.component.aia.itpf.common.event.handler.EventSubscriber;

public class StubbedContext implements EventHandlerContext {

    private final Configuration configuration;
    private Collection<EventSubscriber> eventSubscribers;

    public StubbedContext(final Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Configuration getEventHandlerConfiguration() {
        return configuration;
    }

    @Override
    public Collection<EventSubscriber> getEventSubscribers() {
        return eventSubscribers;
    }

    public void setEventSubscribers(final Collection<EventSubscriber> eventSubscribers) {
        this.eventSubscribers = eventSubscribers;
    }

    public void addEventSubscriber(final EventSubscriber eventSubscriber) {
        if (eventSubscribers == null) {
            eventSubscribers = new ArrayList<EventSubscriber>();
        }
        eventSubscribers.add(eventSubscriber);
    }

    @Override
    public void sendControlEvent(final ControlEvent controlEvent) {

    }

    @Override
    public Object getContextualData(final String arg0) {
        return null;
    }

}
