/*
 * ------------------------------------------------------------------------------
 * *****************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 * *****************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.component.aia.services.exteps.io.adapter.ipl;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import java.util.ArrayList;
import java.util.Collection;
import org.mockito.Mockito;

import com.codahale.metrics.Meter;
import com.ericsson.component.aia.itpf.common.config.Configuration;
import com.ericsson.component.aia.itpf.common.event.ControlEvent;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.component.aia.itpf.common.event.handler.EventSubscriber;
import com.ericsson.component.aia.services.eps.statistics.EpsStatisticsRegister;

public class StubbedContext implements EventHandlerContext {

    private final Configuration configuration;

    private Collection<EventSubscriber> eventSubscribers;

    public StubbedContext(final Configuration configuration) {
        this.configuration = configuration;
        this.eventSubscribers = new ArrayList<>();
    }

    @Override
    public Configuration getEventHandlerConfiguration() {
        return configuration;
    }

    @Override
    public Collection<EventSubscriber> getEventSubscribers() {
        return eventSubscribers;
    }

    @Override
    public void sendControlEvent(final ControlEvent controlEvent) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Object getContextualData(final String data) {

        final EpsStatisticsRegister mockEpsStatisticsRegister = mock(EpsStatisticsRegister.class);
        doReturn(true).when(mockEpsStatisticsRegister).isStatisticsOn();
        doReturn(new Meter()).when(mockEpsStatisticsRegister).createMeter(Mockito.anyString());

        return mockEpsStatisticsRegister;
    }
}