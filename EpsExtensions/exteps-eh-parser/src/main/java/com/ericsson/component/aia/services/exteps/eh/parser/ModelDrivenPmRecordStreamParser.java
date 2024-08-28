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
package com.ericsson.component.aia.services.exteps.eh.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.mediation.parsers.exception.ParsingFailedException;
import com.ericsson.component.aia.mediation.parsers.parser.Parser;
import com.ericsson.component.aia.mediation.parsers.parser.StreamParser;
import com.ericsson.component.aia.model.base.config.bean.SchemaProviderType;

/**
 * This class extends {@link com.ericsson.component.aia.services.exteps.eh.parser.PmRecordStreamParser} It creates a Parser that can be dynamically
 * updated with models from a given filepath
 */
public class ModelDrivenPmRecordStreamParser extends PmRecordStreamParser {

    public static final String MODELS_FILEPATH = "models_filepath";

    public static final String SCHEMA_CHECK_INTERVAL = "schema_check_interval_milliseconds";

    public static final long DEFAULT_SCHEMA_CHECK_INTERVAL = 60000;

    private static final Logger LOGGER = LoggerFactory.getLogger(ModelDrivenPmRecordStreamParser.class);

    protected String modelsFilepath;

    protected long schemaCheckInterval;

    /**
     * Invoked only once during initialization but before any event processing.
     */
    @Override
    protected void doInit() {
        super.doInit();
        modelsFilepath = getStringProperty(MODELS_FILEPATH);
        final String schemaCheckIntervalString = getStringProperty(SCHEMA_CHECK_INTERVAL);
        try {
            schemaCheckInterval = Long.parseLong(schemaCheckIntervalString);
        } catch (final NumberFormatException numberFormatException) {
            LOGGER.error("schemaCheckInterval {} is not a valid number, using default {} instead.", schemaCheckIntervalString,
                    DEFAULT_SCHEMA_CHECK_INTERVAL);
            schemaCheckInterval = DEFAULT_SCHEMA_CHECK_INTERVAL;
        }
    }

    /**
     * Creates a new Parser instance that is dynamically updated with models from a given filepath defined in models_filepath in flow.xml.
     *
     * @return Parser
     */
    @Override
    protected Parser getParser() {
        StreamParser streamParser = null;
        try {
            streamParser = new StreamParser(schemaType, SchemaProviderType.MODEL_DRIVEN_FILE_BASED_ON_DEMAND, getEventBeanListener(), subNetworkName,
                    timezone, outputType, modelsFilepath, schemaCheckInterval);
        } catch (final ParsingFailedException parsingFailedException) {
            LOGGER.error("Failed to create StreamParser object: {}", parsingFailedException);
        }
        return streamParser;
    }

    public long getSchemaCheckInterval() {
        return schemaCheckInterval;
    }
}
