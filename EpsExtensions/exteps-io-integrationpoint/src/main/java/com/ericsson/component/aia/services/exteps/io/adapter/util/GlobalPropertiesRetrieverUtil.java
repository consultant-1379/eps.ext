/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2019
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.exteps.io.adapter.util;

import static com.ericsson.component.aia.services.exteps.io.adapter.common.Constants.GLOBAL_PROPERTIES_CONFIG;
import static com.ericsson.component.aia.services.exteps.io.adapter.common.Constants.GLOBAL_PROPERTIES_HOME;
import static com.ericsson.component.aia.services.exteps.io.adapter.common.Constants.DEFAULT_KAFKA_BROKERS_ADDRESSES_FILE;
import com.ericsson.component.aia.itpf.common.config.Configuration;
/**
 * The Class GlobalPropertiesRetrieverUtil, contains common utility methods.
 */
public class GlobalPropertiesRetrieverUtil {
    private GlobalPropertiesRetrieverUtil(){
        //To Do
    }
    /** RetrieveGlobalPropertiesUtil
     * Returns global.properties path.
     *
     * @param configuration
     *            eps configuration
     *
     * @return String
     *             path
     */
    public static String getFilePath(Configuration configuration ) {
        final String configGlobalPropertiesFilePath = configuration.getStringProperty(GLOBAL_PROPERTIES_CONFIG);
        if (configGlobalPropertiesFilePath == null) {
            return System.getProperty(GLOBAL_PROPERTIES_HOME, DEFAULT_KAFKA_BROKERS_ADDRESSES_FILE);
        }
        return configGlobalPropertiesFilePath;
    }
}
