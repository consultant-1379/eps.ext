/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2018
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.exteps.eh.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;

/**
 * The <code>FileInformationRetrieverUtil</code> contains logic to parse the various types of input.
 */
public final class FileInformationRetrieverUtil {
    private static final Logger log = LoggerFactory.getLogger(FileInformationRetrieverUtil.class);
    private static final String PATH = "path";
    private static final Gson jsonParser = new Gson();

    /**
     * The method try to extract the information based on input type. It will handle following input type
     * <ol>
     * <li>JsonString "{'path' : '/tmp/enm/ebrs/abc-pqr.bin'}" representing com.fasterxml.jackson.databind.JsonNode object</li>
     * <li>JsonString "{'path' : '/tmp/enm/ebrs/abc-pqr.bin'}" representing java string object</li>
     * <li>java.io.File object representing file location</li>
     * <li>Collection<java.io.File> object representing collection of file's</li>
     * <ol>
     *
     * @param parseInput
     *            input representing any of the supporting type.
     * @return collection object
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Collection<File> getListOfFiles(final Object parseInput) {

        if (parseInput instanceof JsonNode) {
            return parseJsonNode((JsonNode) parseInput);
        } else if (parseInput instanceof String) {
            return parseJsonString((String) parseInput);
        } else if (parseInput instanceof File) {
            return Arrays.asList((File) parseInput);
        } else if (parseInput instanceof Collection) {
            return (Collection) parseInput;
        } else {
            log.warn("Unknown type object received  {}", parseInput);
        }
        return null;
    }

    /**
     * The method parse the JsonNode object and extract the file information. <br>
     *
     * @param input
     *            collection of input files if valid input else return empty collection.
     */
    private static Collection<File> parseJsonNode(final JsonNode path) {
        final List<File> files = new ArrayList<>();
        final JsonNode jsonNode = path.get(PATH);
        if (jsonNode != null) {
            final String asText = jsonNode.asText();
            if (asText != null && !asText.isEmpty()) {
                files.add(new File(asText));
            }
        }
        return files;
    }

    /**
     * This method will parse the input json format and convert to File objects. <br>
     *
     * @param input
     *            String represent json object.
     *
     * @return Collection of files if valid input else return empty collection.
     */
    private static Collection<File> parseJsonString(final String input) {
        final List<File> files = new ArrayList<>();
        if (!(input == null || input.isEmpty())) {
            @SuppressWarnings("unchecked")
            final Map<String, String> fromJson = jsonParser.fromJson(input, Map.class);
            final String path = fromJson.get(PATH);
            if (path != null) {
                final String trim = path.trim();
                if (!trim.isEmpty()) {
                    files.add(new File(path));
                }
            }
        }
        return files;
    }

}
