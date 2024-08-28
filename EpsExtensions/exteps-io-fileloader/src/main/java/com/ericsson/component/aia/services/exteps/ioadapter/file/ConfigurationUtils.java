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
package com.ericsson.component.aia.services.exteps.ioadapter.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.itpf.common.config.Configuration;

/**
 * This class reads a string value from the flow.xml file, parses it and returns an int value.
 *
 * @since 0.0.1-SNAPSHOT
 */
public class ConfigurationUtils {
    protected static final Logger LOG = LoggerFactory.getLogger(ConfigurationUtils.class);

    protected static int getIntegerConfigurationParamIfExists(final Configuration config, final String configParamName, final int defaultValue) {
        try {
            return getParameterValueGreaterThanZero(config, configParamName, defaultValue);
        } catch (final Exception e) {
            LOG.error("Exception while parsing {}. Details {}. Will use default value {}", configParamName, e.getMessage(), defaultValue);
            return defaultValue;
        }
    }

    protected static String getConfigurationParamIfExists(final Configuration config, final String configParamName, final String defaultValue) {
        return getParameterValue(config, configParamName, defaultValue);
    }

    protected static boolean getBooleanConfigurationParamIfExists(final Configuration config, final String configParamName, final boolean defaultValue) {
        final Boolean booleanProperty = config.getBooleanProperty(configParamName);
        if (booleanProperty == null) {
            return defaultValue;
        }
        return booleanProperty;
    }

    /**
     * @param config
     * @param configParamName
     */
    private static int getParameterValueGreaterThanZero(final Configuration config, final String configParamName, final int defaultValue) {
        final String numAsString = config.getStringProperty(configParamName);

        if (validParameterValue(numAsString)) {
            LOG.debug("Found {} = {}. Will try to parse it to integer value", configParamName, numAsString);

            final int val = Integer.parseInt(numAsString);
            if (val > 0) {
                LOG.info("{} set to configured value {}", configParamName, val);
                return val;
            }
        }
        return defaultValue;
    }

    /**
     * @param config
     * @param configParamName
     */
    private static String getParameterValue(final Configuration config, final String configParamName, final String defaultValue) {
        final String propertyVal = config.getStringProperty(configParamName);

        if (validParameterValue(propertyVal)) {
            return propertyVal;
        }
        return defaultValue;
    }

    /**
     * @param value
     * @return
     */
    private static boolean validParameterValue(final String value) {
        return value != null && !value.trim().isEmpty();
    }
}
