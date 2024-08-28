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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.component.aia.services.exteps.io.adapter.streaming.StreamingConfig;

@RunWith(MockitoJUnitRunner.class)
public class StreamingPropertyValidatorTest {

    public static class StreamingPropertyValidatorTestData {

        public final static String UNKNOWN_PARAM = "unknownParam";
        public final static String ANY_PARAM_NAME = "aParamName";
        public final static String INPUT_IP = "inputIP";
        public final static String INPUT_IP_VALUE = "1.1.1.1";
        public final static String INPUT_PORT = "inputPort";
        public final static String INPUT_PORT_VALUE = "12345";
        public final static String USER_ID = "userId";
        public final static String USER_ID_VALUE = "123";
        public final static String FILTER_ID = "FilterId";
        public final static String FILTER_ID_VALUE = "456";
        public final static String GROUP_ID = "GroupId";
        public final static String GROUP_ID_VALUE = "789";

        public final static String STREAM_LOAD_MONITOR = "StreamLoadMonitor";
        public final static String STREAM_LOAD_MONITOR_VALUE = "true";

        public final static String MONITOR_PERIOD = "MonitorPeriod";
        public final static String MONITOR_PERIOD_VALUE = "10";
        public final static String TRANSPORT_NAME_VALUE = "NIO_SOCKET_CLIENT";
        public final static int DEFAULT_CONNECTION_RETRIES = 4;
        public static String paramValue = "Anything";

        private final static String COMPONENT_ARRAY_NAME = "componentArray";

        private final static String COMPONENT_ARRAY_VALUE = "com.ericsson.component.aia.mediation.netty.component.multiplex.decoder.MultiplexDecoderComponent,"
                + "com.ericsson.component.aia.mediation.netty.component.multiplex.handshake.MultiplexClientHandshakeComponent,"
                + "com.ericsson.component.aia.services.exteps.io.adapter.streaming.config.GenericStreamingComponent,"
                + "com.ericsson.component.aia.mediation.netty.component.discard.ReleaseBufferComponent";
        private static final String COMPONENT_ARRAY_DELIMITER = ",";
    }

    public Map<String, Object> properties;
    public StreamingPropertyValidatorTestData data = new StreamingPropertyValidatorTestData();

    @Before
    public void setup() {
        properties = new HashMap<String, Object>();
        properties.put(StreamingPropertyValidatorTestData.INPUT_IP, StreamingPropertyValidatorTestData.INPUT_IP_VALUE);
        properties.put(StreamingPropertyValidatorTestData.INPUT_PORT, StreamingPropertyValidatorTestData.INPUT_PORT_VALUE);
        properties.put(StreamingPropertyValidatorTestData.USER_ID, StreamingPropertyValidatorTestData.USER_ID_VALUE);
        properties.put(StreamingPropertyValidatorTestData.FILTER_ID, StreamingPropertyValidatorTestData.FILTER_ID_VALUE);
        properties.put(StreamingPropertyValidatorTestData.GROUP_ID, StreamingPropertyValidatorTestData.GROUP_ID_VALUE);
        properties.put(StreamingPropertyValidatorTestData.STREAM_LOAD_MONITOR, StreamingPropertyValidatorTestData.STREAM_LOAD_MONITOR_VALUE);
        properties.put(StreamingPropertyValidatorTestData.MONITOR_PERIOD, StreamingPropertyValidatorTestData.MONITOR_PERIOD_VALUE);
        properties.put(StreamingPropertyValidatorTestData.COMPONENT_ARRAY_NAME, StreamingPropertyValidatorTestData.COMPONENT_ARRAY_VALUE);
    }

    @Test
    public void testValidate_returnValidConfig() {
        final StreamingConfig config = StreamingPropertyValidator.validateInputAdapter(properties, false);
        assertEquals(config.getHost(), StreamingPropertyValidatorTestData.INPUT_IP_VALUE);
        assertEquals(config.getPort(), Integer.parseInt(StreamingPropertyValidatorTestData.INPUT_PORT_VALUE));
        assertEquals(config.getUserId(), Integer.parseInt(StreamingPropertyValidatorTestData.USER_ID_VALUE));
        assertEquals(config.getFilterId(), Integer.parseInt(StreamingPropertyValidatorTestData.FILTER_ID_VALUE));
        assertEquals(config.getGroupId(), Integer.parseInt(StreamingPropertyValidatorTestData.GROUP_ID_VALUE));
        assertEquals(config.isMonitorOn(), Boolean.parseBoolean(StreamingPropertyValidatorTestData.STREAM_LOAD_MONITOR_VALUE));
        assertEquals(config.getMonitorPeriod(), Integer.parseInt(StreamingPropertyValidatorTestData.MONITOR_PERIOD_VALUE));
        assertEquals(config.getTransportName(), StreamingPropertyValidatorTestData.TRANSPORT_NAME_VALUE);
        assertEquals(config.getConnectionRetries(), StreamingPropertyValidatorTestData.DEFAULT_CONNECTION_RETRIES);
        final String[] componentArrayValueAsStringArray = StreamingPropertyValidatorTestData.COMPONENT_ARRAY_VALUE
                .split(StreamingPropertyValidatorTestData.COMPONENT_ARRAY_DELIMITER);
        assertArrayEquals(config.getComponentArray(), componentArrayValueAsStringArray);
        assertEquals(config.isStatisticsOn(), false);
    }

    @Test
    public void testValidate_returnArrayWithSingleEmptyElement_whenComponentArrayIsNotPresentInProperties() {
        properties.remove(StreamingPropertyValidatorTestData.COMPONENT_ARRAY_NAME);
        final StreamingConfig config = StreamingPropertyValidator.validateInputAdapter(properties, false);
        final String[] componentArrayValueAsStringArray = StreamingConfig.NO_COMPONENT_ARRAY_DEFINED_BY_USER;
        assertArrayEquals(config.getComponentArray(), componentArrayValueAsStringArray);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateStringParameter_whenPropIsNULL_throwIllegalArgumentException() {
        StreamingPropertyValidator.validateStringParameter(properties, StreamingPropertyValidatorTestData.UNKNOWN_PARAM);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateStringParameter_whenPropIsNotString_throwIllegalArgumentException() {
        final int val = 1;
        properties.put(StreamingPropertyValidatorTestData.ANY_PARAM_NAME, val);
        StreamingPropertyValidator.validateStringParameter(properties, StreamingPropertyValidatorTestData.ANY_PARAM_NAME);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateStringParameter_whenPropIsEmpty_throwIllegalArgumentException() {
        properties.put(StreamingPropertyValidatorTestData.ANY_PARAM_NAME, " ");
        StreamingPropertyValidator.validateStringParameter(properties, StreamingPropertyValidatorTestData.ANY_PARAM_NAME);
    }

    @Test
    public void testValidateStringParameter_returnValue() {
        properties.put(StreamingPropertyValidatorTestData.ANY_PARAM_NAME, StreamingPropertyValidatorTestData.paramValue);
        final String value = StreamingPropertyValidator.validateStringParameter(properties, StreamingPropertyValidatorTestData.ANY_PARAM_NAME);
        assertEquals(StreamingPropertyValidatorTestData.paramValue, value);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateInteger_whenPropIsNULL_throwIllegalArgumentException() {
        StreamingPropertyValidator.validateIntegerParameter(properties, StreamingPropertyValidatorTestData.UNKNOWN_PARAM);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateInteger_whenPropIsNotInteger_throwIllegalArgumentException() throws InterruptedException {
        StreamingPropertyValidatorTestData.paramValue = "true";
        properties.put(StreamingPropertyValidatorTestData.ANY_PARAM_NAME, StreamingPropertyValidatorTestData.paramValue);
        StreamingPropertyValidator.validateIntegerParameter(properties, StreamingPropertyValidatorTestData.ANY_PARAM_NAME);
        Thread.sleep(10);
    }

    @Test
    public void testValidateInteger_returnValue() {
        StreamingPropertyValidatorTestData.paramValue = "1";
        properties.put(StreamingPropertyValidatorTestData.ANY_PARAM_NAME, StreamingPropertyValidatorTestData.paramValue);
        final int value = StreamingPropertyValidator.validateIntegerParameter(properties, StreamingPropertyValidatorTestData.ANY_PARAM_NAME);
        assertEquals(Integer.parseInt(StreamingPropertyValidatorTestData.paramValue), value);
    }

    @Test
    public void testValidateInteger_whenPropIsNULL_returnDefaultValue() {
        final int defaultValue = 2;
        final int value = StreamingPropertyValidator.validateIntegerParameter(properties, StreamingPropertyValidatorTestData.UNKNOWN_PARAM,
                defaultValue);
        assertEquals(defaultValue, value);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateIntegerDefault_whenPropIsNotInteger_throwIllegalArgumentException() {
        final int defaultValue = 2;
        StreamingPropertyValidatorTestData.paramValue = "true";
        properties.put(StreamingPropertyValidatorTestData.ANY_PARAM_NAME, StreamingPropertyValidatorTestData.paramValue);
        StreamingPropertyValidator.validateIntegerParameter(properties, StreamingPropertyValidatorTestData.ANY_PARAM_NAME, defaultValue);
    }

    @Test
    public void testValidateIntegerDefault_returnValue() {
        final int defaultValue = 2;
        StreamingPropertyValidatorTestData.paramValue = "1";
        properties.put(StreamingPropertyValidatorTestData.ANY_PARAM_NAME, StreamingPropertyValidatorTestData.paramValue);
        final int value = StreamingPropertyValidator.validateIntegerParameter(properties, StreamingPropertyValidatorTestData.ANY_PARAM_NAME,
                defaultValue);
        assertEquals(Integer.parseInt(StreamingPropertyValidatorTestData.paramValue), value);
    }

    @Test
    public void testValidateBooleanParameter_whenPropIsNULL_returnDefaultValue() {
        final boolean defaultValue = true;
        final boolean value = StreamingPropertyValidator.validateBooleanParameter(properties, StreamingPropertyValidatorTestData.UNKNOWN_PARAM,
                defaultValue);
        assertEquals(defaultValue, value);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateBooleanParameter_whenPropIsNotBoolean_throwIllegalArgumentException() {
        final boolean defaultValue = true;
        StreamingPropertyValidatorTestData.paramValue = "12345";
        properties.put(StreamingPropertyValidatorTestData.ANY_PARAM_NAME, Integer.parseInt(StreamingPropertyValidatorTestData.paramValue));
        StreamingPropertyValidator.validateBooleanParameter(properties, StreamingPropertyValidatorTestData.ANY_PARAM_NAME, defaultValue);
    }

    @Test
    public void testValidateBooleanParameter_returnValue() {
        final boolean defaultValue = false;
        StreamingPropertyValidatorTestData.paramValue = "true";
        properties.put(StreamingPropertyValidatorTestData.ANY_PARAM_NAME, StreamingPropertyValidatorTestData.paramValue);
        final boolean value = StreamingPropertyValidator.validateBooleanParameter(properties, StreamingPropertyValidatorTestData.ANY_PARAM_NAME,
                defaultValue);
        assertEquals(Boolean.parseBoolean(StreamingPropertyValidatorTestData.paramValue), value);
    }
}
