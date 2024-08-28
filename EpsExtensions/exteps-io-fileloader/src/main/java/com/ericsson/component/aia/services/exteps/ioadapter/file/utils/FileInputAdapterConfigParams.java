/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.exteps.ioadapter.file.utils;

public interface FileInputAdapterConfigParams {
    String FILE_REGEX_PROP_NAME = "fileRegex";

    String PARENT_DIRECTORY_PATH_PROP_NAME = "parentDirectoryPath";

    String DIRECTORY_LIST_PROP_NAME = "directoryList";

    String INITIAL_DELAY_PROP_NAME = "intialDelayMilliseconds";

    String INTERVAL_VALUE_PROP_NAME = "intervalValueMilliseconds";

    String ANY_FILE_REGEX = ".*";

    int DEFAULT_MAX_LAST_FILE_SIZE = 30000;

    int DEFAULT_LAST_FILE_REDUCTION_PERCENTAGE = 30;

    String URI = "fileCollection:/";

    String MAX_LAST_FILE_SIZE_PROP_NAME = "lastFileSize";

    String LAST_FILE_REDUCTION_PERCENTAGE = "reductionPercentage ";

    String IGNORE_DOT_LAST_PROP_NAME = "ignoreDotLastFile";

    int MINUTE_IN_MILLISECONDS = 60000;

    int DEFAULT_INTERVAL_VALUE = 5 * MINUTE_IN_MILLISECONDS;

    int DEFAULT_INITIAL_DELAY = 0;
}
