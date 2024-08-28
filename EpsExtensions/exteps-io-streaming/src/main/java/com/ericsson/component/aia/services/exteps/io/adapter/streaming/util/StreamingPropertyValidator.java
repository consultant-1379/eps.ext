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

package com.ericsson.component.aia.services.exteps.io.adapter.streaming.util;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.exteps.io.adapter.streaming.StreamingConfig;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.config.ComponentArray;

public class StreamingPropertyValidator {

    public static final String INPUT_IP = "inputIP";
    public static final String INPUT_PORT = "inputPort";
    public static final String USER_ID = "userId";
    public static final int DEFAULT_USER_ID = 1;
    public static final String FILTER_ID = "FilterId";
    public static final int DEFAULT_FILTER_ID = 0;
    public static final String GROUP_ID = "GroupId";
    public static final int DEFAULT_GROUP_ID = 2;
    public static final String STREAM_LOAD_MONITOR = "StreamLoadMonitor";
    public static final String COMPONENT_ARRAY = "componentArray";
    public static final String TRANSPORT_NAME = "transportName";
    public static final String CONNECTION_RETRIES = "connectionRetries";

    public static final boolean DEFAULT_STREAM_LOAD_MONITOR = false;
    public static final String MONITOR_PERIOD = "MonitorPeriod";
    public static final int DEFAULT_MONITOR_PERIOD = 10000;
    protected static final Logger log = LoggerFactory.getLogger(StreamingPropertyValidator.class);

    private static final int DEFAULT_CONNECTION_RETRIES = 4;
    private static final String CONNECTION_KEEPALIVE = "enableKeepAlive";
    private static final boolean DEFAULT_CONNECTION_KEEPALIVE = false;
    private static final String DEFAULT_COMPONENT_ARRAY_VALUE = "";
    private static final String COMPONENT_ARRAY_DELIMITER = ",";

    public static StreamingConfig validateInputAdapter(final Map<String, Object> properties, final boolean isStatisticsOn) {
        final StreamingConfig config = validate(properties, isStatisticsOn);
        if (config != null) {
            config.setHost(validateStringParameter(properties, INPUT_IP));
            config.setPort(validateIntegerParameter(properties, INPUT_PORT));
        }
        return config;
    }

    public static StreamingConfig validate(final Map<String, Object> properties, final boolean isStatisticsOn) {
        if (properties == null || properties.isEmpty()) {
            return null;
        }
        final StreamingConfig config = validateConfig(properties);
        config.setMonitorOn(validateBooleanParameter(properties, STREAM_LOAD_MONITOR, DEFAULT_STREAM_LOAD_MONITOR));
        config.setMonitorPeriod(validateIntegerParameter(properties, MONITOR_PERIOD, DEFAULT_MONITOR_PERIOD));
        config.setStatisticsOn(isStatisticsOn);
        return config;
    }

    public static StreamingConfig validateDynamicParameters(final Map<String, Object> properties) {
        if (properties == null || properties.isEmpty()) {
            return null;
        }
        return validateConfig(properties);
    }

    private static StreamingConfig validateConfig(final Map<String, Object> properties) {
        final StreamingConfig config = new StreamingConfig();
        config.setUserId(validateIntegerParameter(properties, USER_ID, DEFAULT_USER_ID));
        config.setFilterId(validateIntegerParameter(properties, FILTER_ID, DEFAULT_FILTER_ID));
        config.setGroupId(validateIntegerParameter(properties, GROUP_ID, DEFAULT_GROUP_ID));
        config.setTransportName(validateStringParameter(properties, TRANSPORT_NAME, ComponentArray.getTransportName()));
        config.setConnectionRetries(validateIntegerParameter(properties, CONNECTION_RETRIES, DEFAULT_CONNECTION_RETRIES));
        config.setConnectionKeepAlive(validateBooleanParameter(properties, CONNECTION_KEEPALIVE, DEFAULT_CONNECTION_KEEPALIVE));
        setComponentArray(config, properties);
        return config;
    }

    private static void setComponentArray(final StreamingConfig config, final Map<String, Object> properties) {
        final String componentArrayValueInProperties = validateStringParameter(properties, COMPONENT_ARRAY, DEFAULT_COMPONENT_ARRAY_VALUE);
        if (DEFAULT_COMPONENT_ARRAY_VALUE.equals(componentArrayValueInProperties)) {
            config.setComponentArray(StreamingConfig.NO_COMPONENT_ARRAY_DEFINED_BY_USER);
        } else {
            config.setComponentArray(componentArrayValueInProperties.split(COMPONENT_ARRAY_DELIMITER));
        }
    }

    protected static String validateStringParameter(final Map<String, Object> properties, final String parameterName, final String defaultValue) {
        log.debug("Trying to find value for configuration parameter {}", parameterName);
        final Object prop = properties.get(parameterName);
        if (prop == null) {
            log.warn("Could not find value for configuration parameter {}, using informed default value of '{}'", parameterName, defaultValue);
            return defaultValue;
        }
        return retrieveStringValueFromProperties(parameterName, prop);
    }

    protected static String validateStringParameter(final Map<String, Object> properties, final String parameterName) {
        log.debug("Trying to find value for configuration parameter {}", parameterName);
        final Object prop = properties.get(parameterName);
        if (prop == null) {
            log.error("Could not find value for configuration parameter {}", parameterName);
            throw new IllegalArgumentException(parameterName + "  could not be found");
        }
        return retrieveStringValueFromProperties(parameterName, prop);
    }

    private static String retrieveStringValueFromProperties(final String parameterName, final Object prop) {
        if (!(prop instanceof String)) {
            log.error(" {} has to be String type", parameterName);
            throw new IllegalArgumentException(parameterName + "  must be String type");
        }
        final String value = (String) prop;
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException(parameterName + " must not be empty");
        }
        return value;
    }

    protected static int validateIntegerParameter(final Map<String, Object> properties, final String parameterName) {
        log.debug("Trying to find value for configuration parameter {}", parameterName);
        final Object prop = properties.get(parameterName);
        if (prop == null) {
            log.error("Could not find value for configuration parameter {}", parameterName);
            throw new IllegalArgumentException(parameterName + "  could not be found");
        }
        int value;
        try {
            value = Integer.parseInt((String) prop);
        } catch (final Exception exception) {
            log.error(" {} has to be Integer type", parameterName);
            throw new IllegalArgumentException(parameterName + "  must be Integer type ", exception);
        }
        return value;
    }

    protected static int validateIntegerParameter(final Map<String, Object> properties, final String parameterName, final int defaultValue) {
        log.debug("Trying to find value for configuration parameter {}", parameterName);
        final Object prop = properties.get(parameterName);
        int value;
        if (prop == null) {
            log.error("Could not find value for configuration parameter {}", parameterName);
            value = defaultValue;
        } else {
            try {
                value = Integer.parseInt((String) prop);
            } catch (final Exception exception) {
                log.error(" {} has to be Integer type", parameterName);
                throw new IllegalArgumentException(parameterName + "  must be Integer type ", exception);
            }

        }
        return value;
    }

    protected static boolean validateBooleanParameter(final Map<String, Object> properties, final String parameterName, final boolean defaultValue) {
        log.debug("Trying to find value for configuration parameter {}", parameterName);
        final Object prop = properties.get(parameterName);
        boolean value;
        if (prop == null) {
            log.error("Could not find value for configuration parameter {}", parameterName);
            value = defaultValue;
        } else {
            try {
                value = Boolean.parseBoolean((String) prop);
            } catch (final Exception exception) {
                log.error(" {} has to be boolean type", parameterName);
                throw new IllegalArgumentException(parameterName + "  must be Short type ", exception);
            }
        }
        return value;
    }

}
