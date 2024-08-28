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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.ericsson.component.aia.services.exteps.ioadapter.file.utils.DotLastFileReader;
import com.ericsson.component.aia.services.exteps.ioadapter.file.utils.DotLastFileWriter;

/**
 * This class will periodically collect files and send it downstream as an event using {@link FileCollectionInputAdapter}
 *
 * @since 0.0.1-SNAPSHOT
 */
public class ScheduledFileCollectorWithDotLastProcessing extends ScheduledFileCollector {
    private static final String LAST_LIST_EXTENSION = ".last";

    protected final int maxLastFileSize;

    protected final Map<String, List<String>> lastFileMap;

    protected final int reductionPercentage;

    public ScheduledFileCollectorWithDotLastProcessing(final String fileRegex, final int maxLastFileSize, final int reductionPercentage,
                                                       final FileCollectionInputAdapter fileCollectionInputAdapter) {
        super(fileRegex, fileCollectionInputAdapter);
        this.maxLastFileSize = maxLastFileSize;
        this.reductionPercentage = reductionPercentage;
        lastFileMap = new HashMap<String, List<String>>();
        readDotLastFilesForAllDirectories(fileCollectionInputAdapter.getConfiguredDirectoryList());
    }

    private void readDotLastFilesForAllDirectories(final List<File> directoryArray) {
        for (final File directory : directoryArray) {
            loadLastFileList(directory);
        }
    }

    @Override
    protected List<File> processFilesInDirectory(final File directory) {
        readDotLastFilesForAllDirectories(fileCollectionInputAdapter.getConfiguredDirectoryList());
        List<File> currentfileList = listFilesInDirectory(directory);
        currentfileList = compareLastList(lastFileMap, directory, currentfileList);
        updateLastFiles(lastFileMap.get(directory.getName()), directory);
        return currentfileList;
    }

    @Override
    protected boolean fileShouldBeCollected(final File file) {
        return !file.isHidden() && !file.getName().contains(LAST_LIST_EXTENSION);
    }

    private List<File> compareLastList(final Map<String, List<String>> lastFileMap, final File directory, final List<File> currentfileList) {
        final List<File> newFilesList = new ArrayList<File>();
        if (lastFileMap.containsKey(directory.getName())) {
            final List<String> dotLastFileList = lastFileMap.get(directory.getName());
            for (final File file : currentfileList) {
                if (!dotLastFileList.contains(file.getAbsolutePath().trim())) {
                    newFilesList.add(file);
                    dotLastFileList.add(file.getAbsolutePath().trim());
                }
            }
            LOGGER.info("Found {} new files for processing", newFilesList.size());
            return newFilesList;
        }

        final List<String> newFiles = new ArrayList<String>();
        for (final File newFile : currentfileList) {
            newFiles.add(newFile.getAbsolutePath());
        }
        lastFileMap.put(directory.getName(), newFiles);
        LOGGER.info("Found {} new files for processing", currentfileList.size());
        return currentfileList;
    }

    private void updateLastFiles(final List<String> lastFileList, final File directory) {
        if (lastFileList.size() > maxLastFileSize) {
            final int reduceByPercent = lastFileList.size() * reductionPercentage / 100;
            final Iterator<String> iter = lastFileList.iterator();
            for (int i = 0; i < reduceByPercent; i++) {
                iter.next();
                iter.remove();
            }
        }
        saveLastFileList(lastFileList, directory);
    }

    /**
     * Save the last list of files to the file cache
     */
    private void saveLastFileList(final List<String> lastFileList, final File directory) {
        LOGGER.debug("Saving .lastFileList to a file");

        final File lastFileListFile = new File(directory.getAbsolutePath() + File.separatorChar + LAST_LIST_EXTENSION);

        final DotLastFileWriter fileWriter = new DotLastFileWriter(lastFileListFile);
        fileWriter.write(lastFileList);

        LOGGER.debug("finished Saving .lastFileList to a file");
    }

    private void loadLastFileList(final File directory) {
        LOGGER.debug("loading .LastFile for directoy {} -->", directory);

        final File lastFileListFile = new File(directory.getAbsolutePath() + File.separatorChar + LAST_LIST_EXTENSION);
        if (lastFileListFile.isFile()) {
            final DotLastFileReader fileReader = new DotLastFileReader(lastFileListFile);
            final List<String> lastFileList = fileReader.read();

            lastFileMap.put(directory.getName(), lastFileList);

            LOGGER.debug("last file list loaded from {}", lastFileListFile);
        } else {
            LOGGER.debug("last file list file not found: '{}' in directory : '{}'", lastFileListFile, directory);
        }
        LOGGER.debug("finished loading .LastFile for directoy {} -->", directory);
    }
}
