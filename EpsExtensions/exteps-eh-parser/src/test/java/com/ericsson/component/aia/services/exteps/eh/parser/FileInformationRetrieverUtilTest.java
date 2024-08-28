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

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The <code>FileInformationRetrieverUtilTest</code> test the file information retrieval utility.
 */
public class FileInformationRetrieverUtilTest {
    protected final static Logger log = LoggerFactory.getLogger(FileInformationRetrieverUtilTest.class);

    private static String PATH_KEY = "path";
    private static String PATH_VALUE = "/tmp/enm/ebrs/abc-pqr.bin";

    @Test
    public void getListOfFiles_JsonNodeObject_validPathValues() {
        final String path = "{\"" + PATH_KEY + "\" : \"" + PATH_VALUE + "\"}";
        final ObjectMapper mapper = new ObjectMapper();
        JsonNode actualObj = null;
        try {
            actualObj = mapper.readTree(path);
        } catch (final IOException e) {
            log.error("Error while parsing Json string for JsonNode", e);
        }
        final Collection<File> parseJsonNode = FileInformationRetrieverUtil.getListOfFiles(actualObj);
        assert (parseJsonNode.size() == 1);
        final Object[] array = parseJsonNode.toArray();
        assert (((File) array[0]).getPath().equalsIgnoreCase(new File(PATH_VALUE).getPath()));
    }

    @Test
    public void getListOfFiles_JsonNodeObject_InvalidPathKey() {
        final String path = "{\"" + PATH_KEY + "1\" : \"" + PATH_VALUE + "\"}";
        final ObjectMapper mapper = new ObjectMapper();
        JsonNode actualObj = null;
        try {
            actualObj = mapper.readTree(path);
        } catch (final IOException e) {
            log.error("Error while parsing Json string for JsonNode", e);
        }
        final Collection<File> parseJsonNode = FileInformationRetrieverUtil.getListOfFiles(actualObj);
        assert (parseJsonNode.size() == 0);
    }

    @Test
    public void getListOfFiles_JsonNodeObject_validPathKeyAndEmaptyPathValue() {
        final String path = "{\"" + PATH_KEY + "\" : \"" + "\"}";
        final ObjectMapper mapper = new ObjectMapper();
        JsonNode actualObj = null;
        try {
            actualObj = mapper.readTree(path);
        } catch (final IOException e) {
            log.error("Error while parsing Json string for JsonNode", e);
        }
        final Collection<File> parseJsonNode = FileInformationRetrieverUtil.getListOfFiles(actualObj);
        assert (parseJsonNode.size() == 0);
    }

    @Test
    public void getListOfFiles_JsonString_validPathValues() {
        final String path = "{\"" + PATH_KEY + "\" : \"" + PATH_VALUE + "\"}";
        final Collection<File> parseJsonNode = FileInformationRetrieverUtil.getListOfFiles(path);
        assert (parseJsonNode.size() == 1);
        final Object[] array = parseJsonNode.toArray();
        assert (((File) array[0]).getPath().equalsIgnoreCase(new File(PATH_VALUE).getPath()));
    }

    @Test
    public void getListOfFiles_JsonString_InvalidPathKey() {
        final String path = "{\"" + PATH_KEY + "1\" : \"" + PATH_VALUE + "\"}";
        final Collection<File> parseJsonNode = FileInformationRetrieverUtil.getListOfFiles(path);
        assert (parseJsonNode.size() == 0);
    }

    @Test
    public void getListOfFiles_JsonString_validPathKeyAndEmaptyPathValue() {
        final String path = "{\"" + PATH_KEY + "\" : \"" + "\"}";
        final Collection<File> parseJsonNode = FileInformationRetrieverUtil.getListOfFiles(path);
        assert (parseJsonNode.size() == 0);
    }

    @Test
    public void getListOfFiles_EmptyJsonString() {
        final String path = "{}";
        final Collection<File> parseJsonNode = FileInformationRetrieverUtil.getListOfFiles(path);
        assert (parseJsonNode.size() == 0);
    }

    @Test
    public void getListOfFiles_FileObject_retrunCollectionWithSingleFile() {
        final Collection<File> parseJsonNode = FileInformationRetrieverUtil.getListOfFiles(new File(PATH_VALUE));
        assert (parseJsonNode.size() == 1);
    }

    @Test
    public void getListOfFiles_CollectionOfFileObject_retrunCollectionOfFiles() {
        final Collection<File> input = new ArrayList<>();
        input.add(new File(PATH_VALUE));
        input.add(new File(PATH_VALUE));
        final Collection<File> parseJsonNode = FileInformationRetrieverUtil.getListOfFiles(input);
        assertTrue("Expected collection size " + input.size(), parseJsonNode.size() == input.size());
    }

    @Test
    public void getListOfFiles_EmptyCollection_retrunCollection() {
        final Collection<File> input = new ArrayList<>();
        final Collection<File> parseJsonNode = FileInformationRetrieverUtil.getListOfFiles(input);
        assertTrue("Expected collection size " + input.size(), parseJsonNode.size() == input.size());
    }

}
