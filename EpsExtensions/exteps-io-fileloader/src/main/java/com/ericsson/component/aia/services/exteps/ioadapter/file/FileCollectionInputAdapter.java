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

import static com.ericsson.component.aia.services.exteps.ioadapter.file.ConfigurationUtils.*;
import static com.ericsson.component.aia.services.exteps.ioadapter.file.utils.FileInputAdapterConfigParams.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler;
import com.ericsson.component.aia.services.eps.EpsEngineConstants;
import com.ericsson.component.aia.itpf.common.io.InputAdapter;
import com.ericsson.component.aia.services.eps.statistics.EpsStatisticsRegister;

/**
 * This class implements EPS {@link InputAdapter}, and will periodically collect files and send it downstream as an event. There is a configurable
 * start delay, a configurable interval delay, a configurable root directory path and configurable sub directories to listen on.
 *
 * The Configurable inputs are defined in the flow.xml file.
 *
 * @since 0.0.1-SNAPSHOT
 */
public class FileCollectionInputAdapter extends AbstractEventHandler implements InputAdapter {

    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    private int initialDelay;

    private int intervalValue;

    private String parentDirectoryPath;

    private String directoryNames;

    private List<File> directoryList;

    private String fileRegex;

    private int maxLastFileSize;

    private int reductionPercentage;

    private Boolean ignoreDotLastFile;

    private EpsStatisticsRegister statisticsRegister;

    private Meter eventMeter;

    private Counter totalEventsSent;

    private final ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);

    @Override
    public boolean understandsURI(final String uri) {
        if (uri == null) {
            return false;
        }
        return uri.equals(URI);
    }

    @Override
    public void onEvent(final Object inputEvent) {
        throw new UnsupportedOperationException(
                "Operation not supported. File input adapter is always entry points on event chain!");
    }

    void sendEvent(final Collection<?> event) {
        sendToAllSubscribers(event);
        if (this.isStatisticsOn()) {
            eventMeter.mark();
            totalEventsSent.inc(event.size());
        }
        LOG.debug("event sent to all subscribers. Event : {}", event);
    }

    @Override
    protected void doInit() {
        try {
            getConfigurationValues();
            createScheduledFileCollector();
            initialiseStatistics();
        } catch (final FileNotFoundException e) {
            LOG.error("Exception while initialising. Details {}", e.getMessage());
        }

    }

    /**
     * Initialise statistics.
     */
    protected void initialiseStatistics() {
        statisticsRegister = (EpsStatisticsRegister) getEventHandlerContext().getContextualData(
                EpsEngineConstants.STATISTICS_REGISTER_CONTEXTUAL_DATA_NAME);
        if (statisticsRegister == null) {
            log.error("statisticsRegister should not be null");
        } else {
            if (statisticsRegister.isStatisticsOn()) {
                eventMeter = statisticsRegister.createMeter("batchesSent", this);
                totalEventsSent = statisticsRegister.createCounter("totalFilesSent", this);
            }
        }
    }

    /**
     * This methods gets all the configuration values from the flow.xml
     *
     * @throws NullPointerException
     * @throws FileNotFoundException
     * @throws DirectoryNotFoundException
     */
    private void getConfigurationValues() throws FileNotFoundException {
        directoryList = new ArrayList<File>();
        parentDirectoryPath = getConfiguration().getStringProperty(PARENT_DIRECTORY_PATH_PROP_NAME);
        directoryNames = getConfiguration().getStringProperty(DIRECTORY_LIST_PROP_NAME);
        initialDelay = getIntegerConfigurationParamIfExists(getConfiguration(), INITIAL_DELAY_PROP_NAME,
                DEFAULT_INITIAL_DELAY);
        intervalValue = getIntegerConfigurationParamIfExists(getConfiguration(), INTERVAL_VALUE_PROP_NAME,
                DEFAULT_INTERVAL_VALUE);
        maxLastFileSize = getIntegerConfigurationParamIfExists(getConfiguration(), MAX_LAST_FILE_SIZE_PROP_NAME,
                DEFAULT_MAX_LAST_FILE_SIZE);
        final int percentage = getIntegerConfigurationParamIfExists(getConfiguration(), LAST_FILE_REDUCTION_PERCENTAGE,
                DEFAULT_LAST_FILE_REDUCTION_PERCENTAGE);
        reductionPercentage = getPercentageToUse(percentage);
        ignoreDotLastFile = getBooleanConfigurationParamIfExists(getConfiguration(), IGNORE_DOT_LAST_PROP_NAME, false);
        final String regex = getConfigurationParamIfExists(getConfiguration(), FILE_REGEX_PROP_NAME, ANY_FILE_REGEX);
        fileRegex = getRegexToUse(regex);
    }

    protected List<File> getConfiguredDirectoryList() {
        if (directoryList.isEmpty()) {
            populateDirectoryList(parentDirectoryPath, directoryNames);
        }
        return directoryList;
    }

    /**
     * @param percentage
     * @return
     */
    protected int getPercentageToUse(final int percentage) {
        return percentage > 0 && percentage <= 100 ? percentage : DEFAULT_LAST_FILE_REDUCTION_PERCENTAGE;
    }

    /**
     * Validate User-defined Regex
     */
    protected String getRegexToUse(final String regex) {
        try {
            Pattern.compile(regex);
        } catch (final PatternSyntaxException exception) {
            LOG.error("Invalid fileRegex defined '{}', using default '{}'", regex, ANY_FILE_REGEX);
            return ANY_FILE_REGEX;
        }
        return regex;
    }

    /**
     * This method creates and runs a scheduled task based on the input from the flow.xml
     */
    private void createScheduledFileCollector() {
        final long intervalMiliseconds = intervalValue;
        final long delayMiliseconds = initialDelay;

        scheduledThreadPool.scheduleAtFixedRate(getFileCollector(), delayMiliseconds, intervalMiliseconds,
                TimeUnit.MILLISECONDS);

        LOG.debug("File collection was scheduled for every {} minute(s)", intervalValue);
    }

    private ScheduledFileCollector getFileCollector() {
        if (ignoreDotLastFile) {
            return new ScheduledFileCollectorNoDotLast(fileRegex, this);
        }
        return new ScheduledFileCollectorWithDotLastProcessing(fileRegex, maxLastFileSize, reductionPercentage, this);
    }

    /**
     * Gets a list of directories based on input from the flow.xml
     *
     * @throws NullPointerException
     * @throws FileNotFoundException
     *
     * @return File[] - A File array of directories
     */
    private void populateDirectoryList(final String parentPath, final String directoryNames) {
        final String[] directoryArray = directoryNames.split("\\s*,\\s*");
        for (final String directory : directoryArray) {
            addDirectoryToList(parentPath, directory);
        }

        if (directoryList.isEmpty()) {
            LOG.warn("No directories were found. Expected {} in {}", directoryNames, parentPath);
        }
    }

    /**
     * This method checks if the given directory exists, if it is a directory and adds the directory to an ArrayList
     *
     * @param parentPath
     * @param directory
     *
     */
    private void addDirectoryToList(final String parentPath, final String directoryAsString) {
        final String directoryPath = parentPath + File.separatorChar + directoryAsString;

        final File directory = new File(directoryPath);

        if (isADirectory(directory)) {
            directoryList.add(directory);
            LOG.debug("Directory Added {}", directoryPath);
        }
    }

    /**
     * @param directory
     * @return
     */
    private boolean isADirectory(final File directory) {
        return directory.exists() && directory.isDirectory();
    }

    /**
     * @return the fileRegex
     */
    public String getFileRegex() {
        return fileRegex;
    }

    /**
     * @return true if statistics is enabled
     */
    protected boolean isStatisticsOn() {
        return (statisticsRegister != null) && statisticsRegister.isStatisticsOn();
    }

    @Override
    public void destroy() {
        scheduledThreadPool.shutdownNow();
    }
}
