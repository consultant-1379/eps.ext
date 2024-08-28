/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.exteps.ioadapter.file.utils;

import java.util.Collection;

import com.ericsson.component.aia.itpf.common.config.Configuration;
import com.ericsson.component.aia.itpf.common.event.ControlEvent;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.component.aia.itpf.common.event.handler.EventSubscriber;

public class StubbedContext implements EventHandlerContext {

   private final Configuration configuration;

    public StubbedContext(final Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Configuration getEventHandlerConfiguration() {
        return configuration;
    }

    @Override
    public Collection<EventSubscriber> getEventSubscribers() {
        return null;
    }

    @Override
    public void sendControlEvent(final ControlEvent controlEvent) {

    }

    @Override
    public Object getContextualData(final String arg0) {
        return null;
    }

}
