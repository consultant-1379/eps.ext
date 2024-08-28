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

import java.util.HashMap;
import java.util.Map;

import com.ericsson.component.aia.itpf.common.config.*;

public class StubbedConfiguration implements Configuration {

    private final Map<String, Object> properties = new HashMap<String, Object>();

    public StubbedConfiguration() {
    }

    public void setProperty(final String key, final Object value){
        properties.put(key, value);
    }

    public void setProperties(final Map<String, Object> props ){
        properties.putAll(props);
    }

    @Override
    public Integer getIntProperty(final String propertyName) {
        return (Integer) properties.get(propertyName);
    }

    @Override
    public String getStringProperty(final String propertyName) {
        return (String) properties.get(propertyName);
    }

    @Override
    public Boolean getBooleanProperty(final String propertyName) {
        return (Boolean) properties.get(propertyName);
    }

    @Override
    public Map<String, Object> getAllProperties() {
        return properties;
    }

}
