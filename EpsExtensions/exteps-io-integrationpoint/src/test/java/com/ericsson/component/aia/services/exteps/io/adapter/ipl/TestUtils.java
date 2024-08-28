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
package com.ericsson.component.aia.services.exteps.io.adapter.ipl;

import static com.ericsson.aia.ipl.model.Constants.KAFKA_BROKERS_ADDRESSES_FILE_PROPERTY_NAME;
import static com.ericsson.component.aia.services.exteps.io.adapter.common.Constants.GLOBAL_NON_DEFAULT_PROPERTIES_TEST_FILE;
import static com.ericsson.component.aia.services.exteps.io.adapter.common.Constants.GLOBAL_PROPERTIES_TEST_FILE;
import static com.ericsson.component.aia.services.exteps.io.adapter.common.Constants.GLOBAL_PROPERTIES_CONFIG;
import static com.ericsson.component.aia.services.exteps.io.adapter.common.Constants.INTEGRATION_POINT_NAME;
import static com.ericsson.component.aia.services.exteps.io.adapter.common.Constants.INTEGRATION_POINT_URI;
import static java.lang.System.getProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.HashMap;
import java.util.Map;
import com.ericsson.component.aia.itpf.common.config.Configuration;
import com.ericsson.component.aia.itpf.common.io.Adapter;
import com.ericsson.component.aia.services.eps.coordination.EpsAdaptiveConfiguration;

public final class TestUtils {

    private TestUtils() {
    }

    public static Configuration createTestConfiguration(final String integrationPointName) {
        final EpsAdaptiveConfiguration config = new EpsAdaptiveConfiguration();
        final Map<String, Object> configMap = new HashMap<>();
        configMap.put(INTEGRATION_POINT_URI, "local://src/test/resources/integrationPoints/");
        configMap.put(INTEGRATION_POINT_NAME, integrationPointName);
        config.setConfiguration(configMap);
        return config;
    }

    public static Configuration createTestConfigurationWithGlobalBrokerPropertiesFile(final String integrationPointName) {
        return createTestData(integrationPointName,GLOBAL_NON_DEFAULT_PROPERTIES_TEST_FILE);
    }

    public static Configuration createTestConfigurationWithoutGlobalBrokerPropertiesFile(final String integrationPointName) {
        return createTestData(integrationPointName,"Default");
    }

    public static Configuration createTestConfigurationWithInvalidGlobalBrokerPropertiesFilePath(final String integrationPointName) {
        return createTestData(integrationPointName,"src/test/resources/global.properties.sample3");
    }


    public static Configuration createTestData(final String integrationPointName, final String globalPropertiesConfig){
        final EpsAdaptiveConfiguration config = new EpsAdaptiveConfiguration();
        final Map<String, Object> configMap = new HashMap<>();
        configMap.put(INTEGRATION_POINT_URI, "local://src/test/resources/integrationPoints/");
        configMap.put(INTEGRATION_POINT_NAME, integrationPointName);
        if(!globalPropertiesConfig.equals("Default"))
            configMap.put(GLOBAL_PROPERTIES_CONFIG,globalPropertiesConfig);
        config.setConfiguration(configMap);
        return config;
    }

    public static void assertValidUri(final Adapter adapter, final String uri) {
        assertFalse(adapter.understandsURI(null));
        assertFalse(adapter.understandsURI(""));
        assertFalse(adapter.understandsURI("generic12:/"));
        assertTrue(adapter.understandsURI(uri));
        assertTrue(adapter.understandsURI(uri + "/123"));
    }


    public static void assertbrokerAddressFileLocationDefault() {
        assertEquals(getProperty(KAFKA_BROKERS_ADDRESSES_FILE_PROPERTY_NAME), GLOBAL_PROPERTIES_TEST_FILE);
    }

    public static void assertbrokerAddressFileLocationCustom() {
        assertEquals(getProperty(KAFKA_BROKERS_ADDRESSES_FILE_PROPERTY_NAME), GLOBAL_NON_DEFAULT_PROPERTIES_TEST_FILE);
    }

    public static void assertbrokerAddressFileLocationannotBeUpdated() {
        assertNotEquals(getProperty(KAFKA_BROKERS_ADDRESSES_FILE_PROPERTY_NAME), GLOBAL_PROPERTIES_TEST_FILE);
    }

}
