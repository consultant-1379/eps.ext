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
package com.ericsson.component.aia.services.exteps.ioadapter.solr;

import java.util.*;

import com.ericsson.component.aia.itpf.common.config.Configuration;
import com.ericsson.component.aia.itpf.common.event.ControlEvent;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.component.aia.itpf.common.event.handler.EventSubscriber;

public class TestSolrOutputEventHandlerContext implements EventHandlerContext, Configuration {

    private final Properties properties;

    public TestSolrOutputEventHandlerContext(final Properties properties) {
        this.properties = properties;
    }

    @Override
    public Configuration getEventHandlerConfiguration() {
        return this;
    }

    @Override
    public Collection<EventSubscriber> getEventSubscribers() {
        return null;
    }

    @Override
    public void sendControlEvent(final ControlEvent controlEvent) {
    }

    @Override
    public Object getContextualData(final String name) {
        return null;
    }

    @Override
    public Integer getIntProperty(final String propertyName) {
        final String strVal = getStringValue(propertyName);
        if (strVal != null) {
            final Integer val = Integer.valueOf(strVal);
            return val;
        }
        return null;
    }

    @Override
    public String getStringProperty(final String propertyName) {
        return getStringValue(propertyName);
    }

    @Override
    public Boolean getBooleanProperty(final String propertyName) {
        final String val = getStringValue(propertyName);
        if (val != null) {
            return Boolean.valueOf(val);
        }
        return null;
    }

    @Override
    public String toString() {
        return "TestSolrOutputEventHandlerContext [properties=" + properties + "]";
    }

    private String getStringValue(final String propName) {
        final String val = properties.getProperty(propName);
        return val;
    }

    @Override
    public Map<String, Object> getAllProperties() {
        final Map<String, Object> props = new HashMap<String, Object>();
        for (final String key : properties.stringPropertyNames()) {
            final Object value = properties.getProperty(key);
            props.put(key, value);
        }
        return props;
    }

}
