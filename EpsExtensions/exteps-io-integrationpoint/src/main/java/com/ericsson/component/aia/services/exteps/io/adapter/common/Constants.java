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
package com.ericsson.component.aia.services.exteps.io.adapter.common;

/**
 * Defines the common property names used in the IPL-EPS extensions.
 */
public final class Constants {

    /** The name of the property which holds the relevant integration point name. */
    public static final String INTEGRATION_POINT_NAME = "integration.point.name";

    /** The name of the property which holds the URI for integration points. */
    public static final String INTEGRATION_POINT_URI = "integration.point.uri";

    /** The default kafka broker addresses file. */
    public static final String DEFAULT_KAFKA_BROKERS_ADDRESSES_FILE = "/ericsson/tor/data/global.properties";

    /** The property containing the global.properties filepath. */
    public static final String GLOBAL_PROPERTIES_HOME = "GLOBAL_PROPERTIES_HOME";

    /** The test global.properties filepath */
    public static final String GLOBAL_PROPERTIES_TEST_FILE = "src/test/resources/global.properties.sample";

    /** The test global.properties filepath other than the default filepath */
    public static final String GLOBAL_NON_DEFAULT_PROPERTIES_TEST_FILE = "src/test/resources/global.properties.sample2";

    /** The name of the property which holds the number of milliseconds to batch events. */
    public static final String BATCH_MS = "batchMs";

    /** The name of the property which holds the location of the global.properties config file. */
    public static final String GLOBAL_PROPERTIES_CONFIG = "globalPropertiesFile";

    private Constants() {
    }
}
