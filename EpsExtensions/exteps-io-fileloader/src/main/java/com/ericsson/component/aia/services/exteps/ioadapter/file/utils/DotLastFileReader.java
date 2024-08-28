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
package com.ericsson.component.aia.services.exteps.ioadapter.file.utils;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DotLastFileReader {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private final File file;

    private FileReader fileReader = null;

    private BufferedReader lastListReader = null;

    public DotLastFileReader(final File file) {
        this.file = file;
    }

    public List<String> read() {
        final ArrayList<String> listOfLines = new ArrayList<String>();
        try {
            fileReader = new FileReader(file);
            lastListReader = new BufferedReader(fileReader);

            String fileName = null;
            while ((fileName = lastListReader.readLine()) != null) {
                fileName = fileName.trim();
                listOfLines.add(fileName);
            }
        } catch (final Exception e) {
            LOGGER.debug("last list file could not be opened: {} ", file, e);
        } finally {
            close(lastListReader);
            close(fileReader);
        }
        return listOfLines;
    }

    private void close(final Closeable reader) {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (final IOException e) {
            LOGGER.error("Could not close file", e);
        }
    }

}
