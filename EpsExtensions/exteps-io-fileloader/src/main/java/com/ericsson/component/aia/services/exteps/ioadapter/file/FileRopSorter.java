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
import java.text.ParseException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class receives a collection of files from {@link FileRopSorterEventHandler} and organizes the collection into a hash map,
 * based on a combination of the ROP time and node name which is set in the {@link Key} class
 *
 * @since 0.0.1-SNAPSHOT
 */
public class FileRopSorter {

    private static final int FIFTEEN_MINUTES = 900000;

    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    /**
     * This method creates a list of files for each ROP & Node name from files fetched from the OSS/ENM machine.
     *
     * @param ArrayList
     *            of files collected
     * @throws Exception
     */
    public Map<Key, ArrayList<File>> sendFilesByROP(final List<File> fileList) throws ParseException {
        final Map<Key, ArrayList<File>> fileROPMap = new HashMap<Key, ArrayList<File>>();

        for (final File file : fileList) {
            addFileToList(fileROPMap, file);
        }

        return fileROPMap;
    }

    /**
     * This method checks if there is an entry in the map for this file, if not it adds one.
     *
     * @param Map
     *            of files where files are sorted based on ROP time and NodeName
     * @param File
     *            to be added to map
     * @param Key
     *            of the map based on file name.
     * @throws ParseException
     */
    private void addFileToList(final Map<Key, ArrayList<File>> fileROPMap, final File file) throws ParseException {
        final Key nodeNameROPTimeKey = getNameTimeKey(file);

        ArrayList<File> fileArrayList = fileROPMap.get(nodeNameROPTimeKey);

        if (fileArrayList == null) {
            fileArrayList = new ArrayList<File>();
            fileROPMap.put(nodeNameROPTimeKey, fileArrayList);
        }

        fileArrayList.add(file);
    }

    /**
     * This method generates a key based on the ROP time and NodeName of a particular file. The ROP time of the file will be calculated to the nearest
     * 15 minutes (900 seconds), this just means that all files in a 15 minute period will be loaded together. The Node Name is the "Subnetwork" +
     * "__" + "MeContext"
     *
     * @param File
     *            to be used
     * @return Key for the file
     */
    private Key getNameTimeKey(final File file) throws ParseException {
        final long ropTime = FileUtils.nameToCalendar(file.getName()).getTimeInMillis() / FIFTEEN_MINUTES * FIFTEEN_MINUTES;
        final String nodeName = FileUtils.getCellTraceNodeNameForKey(file.getName());

        final Key nodeNameROPTimeKey = new Key(nodeName, ropTime);

        return nodeNameROPTimeKey;
    }

}
