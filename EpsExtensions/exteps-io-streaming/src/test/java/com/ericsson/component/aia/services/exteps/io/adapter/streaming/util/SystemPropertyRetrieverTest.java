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

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Test;

public class SystemPropertyRetrieverTest {

    private final static String OUTPUT_IP = "outputIP";
    private final static String OUTPUT_IP_VALUE = "2.2.2.2";
    private final static String OUTPUT_PORT = "outputPort";
    private final static String OUTPUT_PORT_STRING_VALUE = "11101";

    @After
    public void teardown() {
        System.getProperties().remove(OUTPUT_IP);
        System.getProperties().remove(OUTPUT_PORT);
    }

    @Test
    public void test_validateStringSystemProperty_validProperty() {
        System.setProperty(OUTPUT_IP, OUTPUT_IP_VALUE);
        final String propertyValue = SystemPropertyRetriever.retrieveStringSystemProperty(OUTPUT_IP);
        assertEquals(OUTPUT_IP_VALUE, propertyValue);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_validateStringSystemProperty_emptyProperty() {
        System.setProperty(OUTPUT_IP, "");
        SystemPropertyRetriever.retrieveStringSystemProperty(OUTPUT_IP);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_validateStringSystemProperty_nullProperty() {
        System.getProperties().remove(OUTPUT_IP);
        SystemPropertyRetriever.retrieveStringSystemProperty(OUTPUT_IP);
    }

    @Test
    public void test_validateIntegerSystemProperty_validProperty() {
        System.setProperty(OUTPUT_PORT, OUTPUT_PORT_STRING_VALUE);
        final int propertyValue = SystemPropertyRetriever.retrieveIntegerSystemProperty(OUTPUT_PORT);
        assertEquals(Integer.parseInt(OUTPUT_PORT_STRING_VALUE), propertyValue);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_validateIntegerSystemProperty_notAnInteger() {
        System.setProperty(OUTPUT_PORT, OUTPUT_IP_VALUE);
        SystemPropertyRetriever.retrieveIntegerSystemProperty(OUTPUT_PORT);
    }

}
