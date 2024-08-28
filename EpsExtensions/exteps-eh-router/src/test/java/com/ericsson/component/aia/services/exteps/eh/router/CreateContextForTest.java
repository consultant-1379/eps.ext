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

package com.ericsson.component.aia.services.exteps.eh.router;

import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.ericsson.component.aia.itpf.common.config.Configuration;
import com.ericsson.component.aia.itpf.common.event.ControlEvent;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.component.aia.itpf.common.event.handler.EventSubscriber;

/**
 * This class creates a generic context for use in multiple test classes
 * 
 */
public class CreateContextForTest {

    public List<EventSubscriber> setUpSubscribers(final int subscriberCount) {

        final List<EventSubscriber> subscribersList = new ArrayList<>();

        for (int i = 0; i < subscriberCount; i++) {

            subscribersList.add(mock(EventSubscriber.class));
        }
        return subscribersList;
    }

    /**
     * Used for creating the context in the FileToLocalOutputRouterTest and FileDistributorEventHandlerTest, test classes.
     * 
     * @param mockedSubscriberList
     * @return
     */
    public EventHandlerContext createContext(final List<EventSubscriber> mockedSubscriberList) {

        final EventHandlerContext ctx = new EventHandlerContext() {

            @Override
            public void sendControlEvent(final ControlEvent ctrl) {

            }

            @Override
            public Configuration getEventHandlerConfiguration() {
                return setupUnusedConfig();
            }

            @Override
            public Collection<EventSubscriber> getEventSubscribers() {
                return mockedSubscriberList;
            }

            @Override
            public Object getContextualData(final String name) {
                return null;
            }
        };
        return ctx;
    }

    /**
     * @return
     */
    private Configuration setupUnusedConfig() {
        final Configuration config = new Configuration() {

            @Override
            public Integer getIntProperty(final String arg0) {
                return null;
            }

            @Override
            public Boolean getBooleanProperty(final String arg0) {
                return null;
            }

            @Override
            public Map<String, Object> getAllProperties() {
                return null;
            }

            @Override
            public String getStringProperty(final String propertyName) {
                return null;
            }
        };
        return config;
    }

}
