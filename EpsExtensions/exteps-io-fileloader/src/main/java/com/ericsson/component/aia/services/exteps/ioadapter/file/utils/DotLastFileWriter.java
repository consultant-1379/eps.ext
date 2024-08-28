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

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DotLastFileWriter {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private final File file;

    private FileOutputStream fileOutputStream = null;

    private PrintWriter lastListWriter = null;

    public DotLastFileWriter(final File file) {
        this.file = file;
    }

    public void write(final List<String> lineList) {
        try {
            fileOutputStream = new FileOutputStream(file);
            lastListWriter = new PrintWriter(fileOutputStream);

            for (final String line : lineList) {
                lastListWriter.println(line);
            }

            lastListWriter.close();

            LOGGER.debug("written {} lines to file {}", lineList.size(), file);
        } catch (final Exception e) {
            LOGGER.debug("last list file could not be created: {}", file, e);
        } finally {
            close(fileOutputStream);
            close(lastListWriter);
        }
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
