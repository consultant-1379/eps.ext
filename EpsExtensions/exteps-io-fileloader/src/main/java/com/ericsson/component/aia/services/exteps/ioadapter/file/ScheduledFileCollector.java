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
package com.ericsson.component.aia.services.exteps.ioadapter.file;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ScheduledFileCollector implements Runnable {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    protected final FileCollectionInputAdapter fileCollectionInputAdapter;

    protected final String fileRegex;

    public ScheduledFileCollector(final String fileRegex, final FileCollectionInputAdapter fileCollectionInputAdapter) {
        this.fileRegex = fileRegex;
        this.fileCollectionInputAdapter = fileCollectionInputAdapter;
    }

    /**
     * List local files under path
     *
     * @param directoryArray The directoryArray to get the file lists for
     * @return ArrayList of directoryArray directly under the paths matching the dirNameSearchPattern's
     */
    public List<File> getFilesFromDirectories(final List<File> directoryArray) {
        final List<File> newFileList = new ArrayList<File>();

        for (final File directory : directoryArray) {
            final List<File> listOfFiles = processFilesInDirectory(directory);
            newFileList.addAll(listOfFiles);
        }

        return newFileList;
    }

    protected List<File> listFilesInDirectory(final File directory) {
        final List<File> currentfileList = new ArrayList<File>();
        for (final File file : directory.listFiles(new RegexFileFilter())) {
            checkIfFileshouldBeCollected(currentfileList, file);
        }
        LOGGER.info("Found {} files in {}", currentfileList.size(), directory);
        return currentfileList;
    }

    private void checkIfFileshouldBeCollected(final List<File> fileList, final File file) {
        if (fileShouldBeCollected(file)) {
            LOGGER.debug("File {} found ", file.getName());
            fileList.add(file);
        }
    }

    /**
     * The implmenting class can decide how to process the files in the given directory.
     *
     * @param directory
     * @return
     */
    protected abstract List<File> processFilesInDirectory(final File directory);

    /**
     * The implementing class can decide the criteria under which the files should be collected.
     *
     * @param file The file that is a candidate for collection
     * @return true if file should be collected, false otherwise
     */
    protected abstract boolean fileShouldBeCollected(final File file);

    @Override
    public void run() {
        try {
            final List<File> fileList = getFilesFromDirectories(fileCollectionInputAdapter.getConfiguredDirectoryList());

            if (fileList.isEmpty()) {
                LOGGER.debug("There are no files for processing");
            } else {
                fileCollectionInputAdapter.sendEvent(fileList);
            }
        } catch (final Exception exception) {
            LOGGER.error("Exception occured while polling directory : ", exception);
        } catch (final Throwable throwable) {
            LOGGER.error("Throwable occured while polling directory : ", throwable);
        }
    }

    class RegexFileFilter implements FileFilter {

        @Override
        public boolean accept(final File pathname) {
            if (pathname.getAbsolutePath().matches(fileRegex)) {
                return true;
            }
            return false;
        }

    }
}