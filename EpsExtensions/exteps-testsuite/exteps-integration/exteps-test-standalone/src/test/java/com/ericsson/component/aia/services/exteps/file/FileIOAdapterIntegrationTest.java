package com.ericsson.component.aia.services.exteps.file;

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
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentMatcher;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;

import com.ericsson.component.aia.services.eps.core.EpsConstants;
import com.ericsson.component.aia.services.eps.core.modules.ModuleManager;
import com.ericsson.component.aia.services.eps.core.util.EpsProvider;
import com.ericsson.component.aia.services.exteps.util.EpsTestUtil;


public class FileIOAdapterIntegrationTest {

    org.slf4j.Logger log = LoggerFactory.getLogger(FileIOAdapterIntegrationTest.class);

    private static final String SUB_FOLDER = "ERBS";
    private static final String FLOW_FILE_DEFAULT = "fileioadapter_test_flow_default.xml";
    private static final String FLOW_FILE_IGNORE_DOT_LAST_FALSE = "fileioadapter_test_flow_with_dotLast.xml";
    private static final String FLOW_FILE_IGNORE_DOT_LAST_TRUE = "fileioadapter_test_flow_default_no_dotlast.xml";
    private static final int FIVE_SECONDS = 5000;
    private static final String FLOW_FILE_DIRECTORY_MARKER = "#########";
    private static final String epsId = "eps01";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private final EpsTestUtil epsTestUtil = new EpsTestUtil();

    private List<String> expectedFileList;

    private ModuleManager moduleManager;

    private Appender<ILoggingEvent> logFileVerifierMock;
    

    @Before
    public void setup() throws InterruptedException, ExecutionException, IOException {
        logFileVerifierMock = getLogFileVerifierMock();
        setupEPSProperties();
        createFlowFiles();
        createFoldersAndFiles();
    }

    @Test
    public void onStartUp_defaultConfiguration_noDotLastExists_shouldcollectAllFilesMatchingRegex() throws IOException, InterruptedException {

        startEpsinstance(FLOW_FILE_DEFAULT);
        verifyResultsAgainstExpected(logFileVerifierMock, expectedFileList, expectedFileList.size(), expectedFileList.size());

    }

    private String getExpectedLogMessage(final List<String> expectedFilesMessage) {
        final StringBuilder builder = new StringBuilder("[");
        for (int x = 0; x < expectedFilesMessage.size(); x++) {
            builder.append(expectedFilesMessage.get(x));
            if (x < expectedFilesMessage.size() - 1) {
                builder.append(", ");
            }
        }
        builder.append("]");
        return builder.toString();
    }

    @Test
    public void afterCollection_defaultConfiguration_withDotLastCreatedAfterFirstCollection_onlyNewFilesShouldBeCollected() throws IOException,
            InterruptedException {
        startEpsinstance(FLOW_FILE_DEFAULT);
        verifyResultsAgainstExpected(logFileVerifierMock, expectedFileList, expectedFileList.size(), expectedFileList.size());

        final List<String> extraFilenames = createFiles("file6.xml", "file7.xml", "file8.xml");
        Thread.sleep(FIVE_SECONDS);
        verifyResultsAgainstExpected(logFileVerifierMock, extraFilenames, expectedFileList.size() + extraFilenames.size(), extraFilenames.size());
    }

    @Test
    public void onStartUP_defaultConfiguration_withDotLastExisting_onlyFilesNotInDotLastShouldBeCollected() throws IOException, InterruptedException {
        createDotLastFile("file1.xml", "file2.xml");
        startEpsinstance(FLOW_FILE_DEFAULT);
        expectedFileList.remove(folder.getRoot().getAbsolutePath() + File.separator + SUB_FOLDER + File.separator + "file1.xml");
        expectedFileList.remove(folder.getRoot().getAbsolutePath() + File.separator + SUB_FOLDER + File.separator + "file2.xml");
        verifyResultsAgainstExpected(logFileVerifierMock, expectedFileList, 5, expectedFileList.size());
    }

    @Test
    public void onStartUp_ignoreDotLastConfiguredFalse_NoDotLastExists_shouldcollectAllFilesMatchingRegex() throws IOException, InterruptedException {
        startEpsinstance(FLOW_FILE_IGNORE_DOT_LAST_FALSE);
        verifyResultsAgainstExpected(logFileVerifierMock, expectedFileList, expectedFileList.size(), expectedFileList.size());
    }

    @Test
    public void afterCollection_ignoreDotLastConfiguredFalse_withDotLastCreatedAfterFirstCollection_onlyNewFilesShouldBeCollected()
            throws InterruptedException, IOException {
        startEpsinstance(FLOW_FILE_IGNORE_DOT_LAST_FALSE);
        verifyResultsAgainstExpected(logFileVerifierMock, expectedFileList, expectedFileList.size(), expectedFileList.size());

        final List<String> extraFilenames = createFiles("file6.xml", "file7.xml", "file8.xml");
        Thread.sleep(FIVE_SECONDS);
        verifyResultsAgainstExpected(logFileVerifierMock, extraFilenames, expectedFileList.size() + extraFilenames.size(), extraFilenames.size());
    }

    @Test
    public void onStartUP_ignoreDotLastConfiguredFalse_withDotLastExisting_onlyFilesNotInDotLastShouldBeCollected() throws IOException,
            InterruptedException {
        createDotLastFile("file1.xml", "file2.xml");
        startEpsinstance(FLOW_FILE_IGNORE_DOT_LAST_FALSE);
        Thread.sleep(FIVE_SECONDS);
        expectedFileList.remove(folder.getRoot().getAbsolutePath() + File.separator + SUB_FOLDER + File.separator + "file1.xml");
        expectedFileList.remove(folder.getRoot().getAbsolutePath() + File.separator + SUB_FOLDER + File.separator + "file2.xml");
        verifyResultsAgainstExpected(logFileVerifierMock, expectedFileList, 5, expectedFileList.size());
    }

    @Test
    public void onStartUp_ignoreDotLastConfiguredTrue_NoDotLastExists_shouldcollectAllFilesMatchingRegex() throws InterruptedException, IOException {
        startEpsinstance(FLOW_FILE_IGNORE_DOT_LAST_TRUE);
        Thread.sleep(FIVE_SECONDS);
        verifyResultsAgainstExpectedForNoDotLastProcessing(logFileVerifierMock, expectedFileList, expectedFileList.size());
    }

    @Test
    public void afterCollection_ignoreDotLastConfiguredTrue_noDotLastFile_allFilesShouldBeCollectedAgain() throws InterruptedException, IOException {
        startEpsinstance(FLOW_FILE_IGNORE_DOT_LAST_TRUE);
        verifyResultsAgainstExpectedForNoDotLastProcessing(logFileVerifierMock, expectedFileList, expectedFileList.size());

        final List<String> extraFilenames = createFiles("file6.xml", "file7.xml", "file8.xml");
        expectedFileList.addAll(extraFilenames);
        Thread.sleep(FIVE_SECONDS);
        verifyResultsAgainstExpectedForNoDotLastProcessing(logFileVerifierMock, expectedFileList, expectedFileList.size());
    }

    @Test
    public void incorrectlyConfiguredDirectoryList_threadShouldStillContinueChecking() throws InterruptedException, IOException {
        deleteSubFolder();
        startEpsinstance(FLOW_FILE_IGNORE_DOT_LAST_TRUE);
        Thread.sleep(FIVE_SECONDS);
        verifyExpectations(logFileVerifierMock, "No directories were found. Expected " + SUB_FOLDER + " in " + folder.getRoot().getAbsolutePath());
    }

    @Test
    public void incorrectlyConfiguredDirectoryList_threadShouldStillContinueChecking_whenFolderCreatedShouldCollectFiles()
            throws InterruptedException, IOException {
        deleteSubFolder();
        startEpsinstance(FLOW_FILE_IGNORE_DOT_LAST_TRUE);
        Thread.sleep(FIVE_SECONDS);
        verifyExpectations(logFileVerifierMock, "No directories were found. Expected " + SUB_FOLDER + " in " + folder.getRoot().getAbsolutePath());
        createFoldersAndFiles();
        Thread.sleep(FIVE_SECONDS);
        verifyResultsAgainstExpectedForNoDotLastProcessing(logFileVerifierMock, expectedFileList, expectedFileList.size());
    }

    @Test
    public void incorrectlyConfiguredDirectoryList_threadShouldStillContinueChecking_whenFolderCreatedAndContainsDotLastShouldCollectFiles()
            throws IOException, InterruptedException {
        deleteSubFolder();
        startEpsinstance(FLOW_FILE_IGNORE_DOT_LAST_FALSE);
        Thread.sleep(FIVE_SECONDS);
        verifyExpectations(logFileVerifierMock, "No directories were found. Expected " + SUB_FOLDER + " in " + folder.getRoot().getAbsolutePath());
        createSubDir(SUB_FOLDER);
        createDotLastFile("file1.xml", "file2.xml");
        createFiles();
        expectedFileList.remove(folder.getRoot().getAbsolutePath() + File.separator + SUB_FOLDER + File.separator + "file1.xml");
        expectedFileList.remove(folder.getRoot().getAbsolutePath() + File.separator + SUB_FOLDER + File.separator + "file2.xml");
        Thread.sleep(FIVE_SECONDS);
        verifyResultsAgainstExpected(logFileVerifierMock, expectedFileList, 5, expectedFileList.size());
    }

    private void deleteSubFolder() {
        final File subFolder = new File(folder.getRoot().getAbsolutePath() + File.separator + SUB_FOLDER);
        deleteAllFilesInFolder(subFolder);
        assertTrue(subFolder.delete());
    }

    private void deleteAllFilesInFolder(final File subFolder) {
        final File[] files = subFolder.listFiles();
        for (final File file : files) {
            assertTrue(file.delete());
        }
    }

	private void startEpsinstance(final String flowFile) throws InterruptedException, IOException {
		final InputStream moduleInputStream = new FileInputStream(folder.getRoot().getPath() + File.separator + flowFile);
		assertNotNull(moduleInputStream);
		final String deployModuleId = epsTestUtil.deployModule(moduleInputStream);
		log.info("Deployed moduleId : '{}'", deployModuleId);
		moduleManager = epsTestUtil.getEpsInstanceManager().getModuleManager();
		assertEquals(1, moduleManager.getDeployedModulesCount());
		Thread.sleep(10000);
	}

    private void setupEPSProperties() throws InterruptedException, ExecutionException {
        System.setProperty("com.ericsson.component.aia.services.eps.module.deployment.folder.path", "src/test/resources");
        System.setProperty("com.ericsson.component.aia.services.eps.deploy.already.existing.modules.on.startup", "false");
        System.setProperty(EpsConstants.EPS_INSTANCE_ID_PROP_NAME, epsId);
        epsTestUtil.createEpsInstanceInNewThread();
    }

    private void createFlowFiles() throws IOException {
        copyFlowFileToTemporaryFolder(FLOW_FILE_DEFAULT);
        copyFlowFileToTemporaryFolder(FLOW_FILE_IGNORE_DOT_LAST_FALSE);
        copyFlowFileToTemporaryFolder(FLOW_FILE_IGNORE_DOT_LAST_TRUE);
    }

    private void copyFlowFileToTemporaryFolder(final String flowFileName) throws IOException {
        final Path flowFile = Paths.get("src/test/resources/flows/" + flowFileName);
        final Path destination = Paths.get(folder.getRoot().getAbsolutePath() + File.separator + flowFileName);
        Files.copy(flowFile, destination);
        updateFlowFileWithTemporaryFolder(flowFileName);
    }

    private void updateFlowFileWithTemporaryFolder(final String flowFileName) throws IOException {
        final Path path = Paths.get(folder.getRoot().getPath() + File.separator + flowFileName);
        String content = new String(Files.readAllBytes(path), UTF_8);
        final String absolutePath = folder.getRoot().getAbsolutePath();
        content = java.util.regex.Pattern.compile(FLOW_FILE_DIRECTORY_MARKER).matcher(content).replaceAll(Matcher.quoteReplacement(absolutePath));
        Files.write(path, content.getBytes(UTF_8));
    }

    private void createFoldersAndFiles() throws IOException, InterruptedException {
        createSubDir(SUB_FOLDER);
        createFiles();
    }

    private void createFiles() throws IOException, InterruptedException {
        expectedFileList = createFiles("file1.xml", "file2.xml", "file3.xml", "file4.xml", "file5.xml");
        createFile("fileThatShouldNotBeCollected.txt");
        createFile("anotherfileThatShouldNotBeCollected.xmle");
        EpsTestUtil.createhiddenFile(folder, "ERBS", "hiddenFile.xml");
    }

    private void createSubDir(final String subDir) {
        try {
            folder.newFolder(subDir);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private List<String> createFiles(final String... fileNames) throws IOException {
        final List<String> fileNameList = new ArrayList<>();
        for (final String fileName : fileNames) {
            final File createdFile = createFile(fileName);
            fileNameList.add(createdFile.getPath());
        }
        return fileNameList;
    }

    private File createFile(final String fileName) throws IOException {
        return folder.newFile(SUB_FOLDER + File.separator + fileName);
    }

    private void createDotLastFile(final String... fileNames) throws IOException, UnsupportedEncodingException {
        final String dotLastDirectory = folder.getRoot().getAbsolutePath() + File.separator + SUB_FOLDER;
        final File dotLast = new File(dotLastDirectory, ".last");
        try (PrintWriter writer = new PrintWriter(dotLast, "UTF-8")) {
            for (final String fileName : fileNames) {
                writer.println(dotLastDirectory + File.separator + fileName);
            }
            writer.flush();
        }
        log.error("Written .lastfile {}", dotLast.getAbsolutePath());
    }

    private void verifyResultsAgainstExpected(final Appender<ILoggingEvent> logFileVerifierMock, final List<String> expectedFileList,
                                              final int totalFiles, final int newFiles) {
        verifyResultsAgainstExpectedForNoDotLastProcessing(logFileVerifierMock, expectedFileList, totalFiles);
        verifyExpectations(logFileVerifierMock, "Found " + newFiles + " new files for processing");
    }

    private void verifyResultsAgainstExpectedForNoDotLastProcessing(final Appender<ILoggingEvent> logFileVerifierMock,
                                                                    final List<String> expectedFileList, final int totalFiles) {
        final String path = folder.getRoot().getAbsolutePath() + File.separator + SUB_FOLDER;
        verifyExpectations(logFileVerifierMock, "Found " + totalFiles + " files in " + path);
        final String expectedLogMessage = getExpectedLogMessage(expectedFileList);
        verifyExpectations(logFileVerifierMock, expectedLogMessage);
    }

	@After
	public void cleanup() throws InterruptedException {
		log.info("Shutting down EPS instance ...");
		epsTestUtil.shutdownEpsInstance();
		log.info("Sleeping for 5 Seconds ...");
		Thread.sleep(5000); // Wait to ensure module is undeployed
	}

    private void verifyExpectations(final Appender<ILoggingEvent> mockAppender, final String name) {
        verify(mockAppender, atLeast(1)).doAppend(calledWithThis(name));
    }

    private ILoggingEvent calledWithThis(final String name) {
        return argThat(new ArgumentMatcherExtension(name));
    }

    private Appender<ILoggingEvent> getLogFileVerifierMock() {
        final Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        final Appender<ILoggingEvent> mockAppender = getAppenderMock();
        when(mockAppender.getName()).thenReturn("MOCK");
        root.addAppender(mockAppender);
        return mockAppender;
    }

    private Appender<ILoggingEvent> getAppenderMock() {
        @SuppressWarnings("unchecked")
        final Appender<ILoggingEvent> mock = mock(Appender.class);
        return mock;
    }

    private final class ArgumentMatcherExtension extends ArgumentMatcher<ILoggingEvent> {

        private final String logEntry;

        public ArgumentMatcherExtension(final String logEntry) {
            this.logEntry = logEntry;
        }

        @Override
        public boolean matches(final Object argument) {
            final String logMessage = ((LoggingEvent) argument).getFormattedMessage();
            if (logMessage.contains("event sent to all subscribers. Event : [")) {
                return getSortedFilenameList(logMessage).contains(logEntry);
            }
            return logMessage.contains(logEntry);
        }

        /*
         * This method is needed because when run in Linux, the order of the filenames in the log message is based on something other than filename.
         * By using this method, we sort the file name based on name and use that to compare against the expecetd value.
         */
        private String getSortedFilenameList(final String logMessage) {
            final List<String> asList = getSortedListOfFileNames(logMessage);
            return getNewLogMessageWithSortedFileNames(logMessage, asList);
        }

        private String getNewLogMessageWithSortedFileNames(final String logMessage, final List<String> asList) {
            final String currentStartOfLogMessage = logMessage.substring(0, logMessage.indexOf("[") + 1);
            final StringBuilder sortedLogMessage = new StringBuilder(currentStartOfLogMessage);
            sortedLogMessage.append(createFileNameList(asList));
            sortedLogMessage.append("]");
            return sortedLogMessage.toString();
        }

        private String createFileNameList(final List<String> asList) {
            final StringBuilder fileNames = new StringBuilder();
            for (int x = 0; x < asList.size(); x++) {
                fileNames.append(asList.get(x));
                if (x < asList.size() - 1) {
                    fileNames.append(", ");
                }
            }
            return fileNames.toString();
        }

        private List<String> getSortedListOfFileNames(final String logMessage) {
            final String filenames = logMessage.substring(logMessage.indexOf("[") + 1, logMessage.indexOf("]"));
            final String[] fileNameArray = filenames.split(", ");
            final List<String> asList = Arrays.asList(fileNameArray);
            Collections.sort(asList);
            return asList;
        }
    }
}
