/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.component.aia.services.exteps.io.adapter.streaming.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemPropertyRetriever {

    public static final Logger LOGGER = LoggerFactory.getLogger(SystemPropertyRetriever.class);

    public static String retrieveStringSystemProperty(final String propertyName) {
        final String propertyValue = System.getProperty(propertyName);
        isPropertyValid(propertyName, propertyValue);
        return propertyValue;
    }

    public static int retrieveIntegerSystemProperty(final String propertyName) {
        final String propertyValue = retrieveStringSystemProperty(propertyName);
        return convertStringToInteger(propertyName, propertyValue);
    }

    private static void isPropertyValid(final String propertyName, final String propertyValue) {
        isPropertyNull(propertyName, propertyValue);
        isPropertyEmpty(propertyName, propertyValue);
    }

    private static void isPropertyNull(final String propertyName, final String propertyValue) {
        if (propertyValue == null) {
            LOGGER.error("Could not find value for configuration property {}", propertyName);
            throw new IllegalArgumentException(propertyName + "  could not be found");
        }
    }

    private static void isPropertyEmpty(final String propertyName, final String propertyValue) {
        if (propertyValue.trim().isEmpty()) {
            throw new IllegalArgumentException(propertyName + " must not be empty");
        }
    }

    private static int convertStringToInteger(final String propertyName, final String propertyValue) {
        int value;
        try {
            value = Integer.parseInt(propertyValue);
        } catch (final NumberFormatException exception) {
            LOGGER.error(" {} has to be a type of Integer", propertyName);
            throw new IllegalArgumentException(propertyName + "  must be a type of Integer ", exception);
        }
        return value;
    }

}
