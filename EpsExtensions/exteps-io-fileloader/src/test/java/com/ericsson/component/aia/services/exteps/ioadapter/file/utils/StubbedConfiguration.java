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

import java.util.HashMap;
import java.util.Map;

import com.ericsson.component.aia.itpf.common.config.Configuration;

public class StubbedConfiguration implements Configuration {

    Map<String, Object> properties = new HashMap<String, Object>();

    public void setStringProperty(final String key, final Object value) {
        properties.put(key, value);
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
        return null;
    }

    @Override
    public Map<String, Object> getAllProperties() {
        return properties;
    }

}
