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

import java.text.*;
import java.util.Calendar;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides static utility methods for getting information from file names
 *
 * @since 0.0.1-SNAPSHOT
 */
public class FileUtils {
    public static final String UNKNOWN_NE_NAME = "unknown";

    public static final String SUB_NETWORK = "SubNetwork=";

    protected static final Logger LOG = LoggerFactory.getLogger(FileUtils.class);

    static final int SHORT_DATE_UNDERSCORE_POS = 19;

    static final int LONG_DATE_LENGTH = 18; // YYYYMMDD.HHMM+HHMM

    static final int SHORT_DATE_LENGTH = 13; // YYYYMMDD.HHMM

    /**
     * Method to get the SubNetwork name from a path string
     *
     * @param pathString
     *            - The string to search for a NE name
     * @return The Node Name name
     */
    public static String getCellTraceNodeNameForKey(final String pathString) {
        int startIndex = pathString.lastIndexOf(SUB_NETWORK);

        if (startIndex == -1) {
            return UNKNOWN_NE_NAME;
        }
        startIndex += SUB_NETWORK.length();

        int endIndex = pathString.indexOf("_celltracefile_", startIndex);

        if (endIndex == -1) {
            endIndex = pathString.indexOf("%~%", startIndex);
            if (endIndex == -1) {
                return UNKNOWN_NE_NAME;
            }
        }

        final String nodeName = pathString.substring(startIndex, endIndex).replace(",MeContext=", "__");
        return nodeName;
    }

    /**
     * Method to get a calendar time from a file name
     *
     * @param The
     *            file name
     * @throws ParseException
     */
    public static Calendar nameToCalendar(final String fileNamePath) throws ParseException {
        final Calendar calendar = Calendar.getInstance(TimeZone.getDefault());

        // Define the format of dates on input files
        final DateFormat longDateFormat = new SimpleDateFormat("yyyyMMdd.HHmmZ");
        final DateFormat shortDateFormat = new SimpleDateFormat("yyyyMMdd.HHmm");

        final String fileName = getFromDatePart(fileNamePath);

        final int underscoreLocation = fileName.indexOf('_');

        if (underscoreLocation > SHORT_DATE_UNDERSCORE_POS) {
            calendar.setTime(longDateFormat.parse(fileName.substring(0, LONG_DATE_LENGTH)));
        } else {
            calendar.setTime(shortDateFormat.parse(fileName.substring(0, SHORT_DATE_LENGTH)));
        }

        return calendar;
    }

    /**
     * Get the file name from the start of the date
     *
     * @param fileNamePath
     *            : The incoming full name
     * @return The date part
     */
    private static String getFromDatePart(final String fileNamePath) {
        if (fileNamePath.charAt(0) == 'A') {
            return fileNamePath.substring(1);
        }

        int startPos = fileNamePath.lastIndexOf("%~%A");
        if (startPos >= 0) {
            return fileNamePath.substring(startPos + 4);
        }

        startPos = fileNamePath.lastIndexOf("/A");
        if (startPos >= 0) {
            return fileNamePath.substring(startPos + 2);
        }

        return fileNamePath;
    }

}