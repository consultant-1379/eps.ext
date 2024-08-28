/*
 * ------------------------------------------------------------------------------
 *  *******************************************************************************
 *  * COPYRIGHT Ericsson 2017
 *  *
 *  * The copyright to the computer program(s) herein is the property of
 *  * Ericsson Inc. The programs may be used and/or copied only with written
 *  * permission from Ericsson Inc. or in accordance with the terms and
 *  * conditions stipulated in the agreement/contract under which the
 *  * program(s) have been supplied.
 *  *******************************************************************************
 *  *----------------------------------------------------------------------------
 */

package com.ericsson.component.aia.epscomponent;

import java.util.ArrayList;
import java.util.Collection;

import org.mockito.Mock;

import com.ericsson.component.aia.itpf.common.config.*;
import com.ericsson.component.aia.itpf.common.event.*;
import com.ericsson.component.aia.itpf.common.event.handler.*;
import com.ericsson.component.aia.services.eps.statistics.*;

public class StubbedContext implements EventHandlerContext {
    private final EpsStatisticsRegister epsStatisticsRegister;

    @Mock
    EventSubscriber mockedSubscriber = new EventSubscriber() {


        @Override
        public void sendEvent(final Object event) {
        }

        @Override
        public String getIdentifier() {
            return "testIdentifier";
        }
    };

    private Configuration configuration;


    StubbedContext(final Configuration configuration, final EventSubscriber subscriber, final EpsStatisticsRegister epsStatisticsRegister) {
        this.configuration = configuration;
        this.mockedSubscriber = subscriber;
        this.epsStatisticsRegister = epsStatisticsRegister;
    }

    @Override
    public Configuration getEventHandlerConfiguration() {
        return configuration;
    }

    @Override
    public Collection<EventSubscriber> getEventSubscribers() {
        final Collection<EventSubscriber> subscribers = new ArrayList<>();
        subscribers.add(mockedSubscriber);
        return subscribers;
    }

    @Override
    public void sendControlEvent(final ControlEvent controlEvent) {

    }

    @Override
    public Object getContextualData(final String arg0) {
        return epsStatisticsRegister;
    }

}
