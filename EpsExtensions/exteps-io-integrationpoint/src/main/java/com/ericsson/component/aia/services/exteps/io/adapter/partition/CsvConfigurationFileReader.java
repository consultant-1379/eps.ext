/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2013
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.component.aia.services.exteps.io.adapter.partition;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class CsvConfigurationFileReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(CsvConfigurationFileReader.class);
    private static final String COMMA = ",";
    private static final String NUMBER_ONLY_REGEX = "[0-9]+";

    private CsvConfigurationFileReader() {

    }

    /**
     * Gets the paired enode b ids and partition numbers from csv file and returns a map of these paired values
     * with the enode b id as the key and the partition number as the value
     * Csv file should contain a pair of comma separated values on each line
     * Example: 1234, 5678
     * where 1234 is the enode b id and 5678 is the partition number
     *
     * @param filepath
     *            the filepath of the csv file
     * @return enodeBIdsToPartition
     */
    public static Map<Integer, Integer> getCsvContent(final String filepath) {

        if (!StringUtils.isBlank(filepath)) {
            final Map<Integer, Integer> enodeBIdsToPartition = new HashMap<>();
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(filepath)))) {
                for (String line; (line = bufferedReader.readLine()) != null;) {
                    final String[] enodeBIds = line.split(COMMA);
                    if (null != enodeBIds) {
                        final String enodeBId = enodeBIds[0].trim();
                        final String partition = enodeBIds[1].trim();
                        if (enodeBId.matches(NUMBER_ONLY_REGEX) && partition.matches(NUMBER_ONLY_REGEX)) {
                            enodeBIdsToPartition.put(Integer.parseInt(enodeBId), Integer.parseInt(partition));
                            LOGGER.debug("enodeBId, PartitionNumber: {}, {}", enodeBIds[0], enodeBIds[1]);
                        }
                    }
                }
                return enodeBIdsToPartition;
            } catch (final IOException e) {
                LOGGER.error("{} was not found", filepath, e);

            }
        }
        LOGGER.warn("Partitioning by File Not in use as there is no value set for csv.filepath, "
                + "partitioning will be done using the kafka partitioning class defined by partitioner.class kafka property");

        return Collections.emptyMap();
    }
}
