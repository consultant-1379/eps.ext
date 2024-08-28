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

import static com.ericsson.component.aia.services.exteps.ioadapter.file.ListMatcherIgnoreOrderOfContents.contentsTheSame;
import static com.ericsson.component.aia.services.exteps.ioadapter.file.ScheduledFileCollectorTestUtilities.createhiddenFile;
import static com.ericsson.component.aia.services.exteps.ioadapter.file.utils.FileInputAdapterConfigParams.ANY_FILE_REGEX;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.component.aia.services.exteps.ioadapter.file.FileCollectionInputAdapter;
import com.ericsson.component.aia.services.exteps.ioadapter.file.ScheduledFileCollector;
import com.ericsson.component.aia.services.exteps.ioadapter.file.ScheduledFileCollectorNoDotLast;

/**
 * This class is a test class for {@link ScheduledFileCollectorNoDotLast}
 * 
 * @since 0.0.1-SNAPSHOT
 */
@RunWith(MockitoJUnitRunner.class)
public class ScheduledFileCollectorNoDotLastProcessingTest {
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	
	@Mock
	private FileCollectionInputAdapter mockFileCollectionInputAdapter;
	
	private static final String LAST_LIST_EXTENSION = ".last";
	
	private List<File> directoryList;
	private File testFileOne;
	private File testFileTwo;
	private File testFileThree;
	
	@Before
	public void setup() throws IOException, InterruptedException {
		testFileOne = folder.newFile("testFileA.txt");
		testFileTwo = folder.newFile("testFileB.txt");
		testFileThree = folder.newFile("testFileC.txt");
		createhiddenFile(folder, "hiddenFile.txt");
		directoryList = new ArrayList<File>();
		directoryList.add(folder.getRoot());
		when(mockFileCollectionInputAdapter.getConfiguredDirectoryList()).thenReturn(directoryList);
	}
	
	@Test
	public void run_NoMatchingRegex_ShouldProcessnothing() {
		final ScheduledFileCollector objUndertest = new ScheduledFileCollectorNoDotLast("\\.nonExistingExt^",
				mockFileCollectionInputAdapter);
		
		objUndertest.run();
		verify(mockFileCollectionInputAdapter).getConfiguredDirectoryList();
		verifyNoMoreInteractions(mockFileCollectionInputAdapter);
	}
	
	@Test
	public void run_firstTime_ShouldProcessAllFiles() {
		final ScheduledFileCollector objUndertest = new ScheduledFileCollectorNoDotLast(ANY_FILE_REGEX,
				mockFileCollectionInputAdapter);
		final List<File> expectedFileList = createSortedExpectedFileList(testFileOne, testFileTwo, testFileThree);
		
		objUndertest.run();
		
		verify(mockFileCollectionInputAdapter).sendEvent(argThat(contentsTheSame(expectedFileList)));
	}
	
	@Test
	public void run_secondRun_shouldProcessFilesAgainBecauseNoDotLastFile() {
		final ScheduledFileCollector objUndertest = new ScheduledFileCollectorNoDotLast(ANY_FILE_REGEX,
				mockFileCollectionInputAdapter);
		
		final List<File> expectedFileList = createSortedExpectedFileList(testFileOne, testFileTwo, testFileThree);
		objUndertest.run();
		objUndertest.run();
		
		verify(mockFileCollectionInputAdapter, times(2)).sendEvent(argThat(contentsTheSame(expectedFileList)));
		
		final File lastFile = new File(directoryList.get(0).getAbsolutePath() + File.separator + LAST_LIST_EXTENSION);
		assertFalse("Could find .last file in " + directoryList.get(0).getAbsolutePath(), lastFile.exists());
	}
	
	@Test
	public void run_secondFileCollectorRun_shouldProcessFilesAgainBecauseNoDotLastFile() {
		final ScheduledFileCollector objUndertest = new ScheduledFileCollectorNoDotLast(ANY_FILE_REGEX,
				mockFileCollectionInputAdapter);
		
		final List<File> expectedFileList = createSortedExpectedFileList(testFileOne, testFileTwo, testFileThree);
		objUndertest.run();
		
		final ScheduledFileCollector objUndertest2 = new ScheduledFileCollectorNoDotLast(ANY_FILE_REGEX,
				mockFileCollectionInputAdapter);
		objUndertest2.run();
		
		verify(mockFileCollectionInputAdapter, times(2)).sendEvent(argThat(contentsTheSame(expectedFileList)));
	}
	
	@Test
	public void run_addmoreFilesAfterFirstRun_checkThatAllFilesAreProcessedAgain()
			throws IOException, InterruptedException {
		final ScheduledFileCollector objUnderTest = new ScheduledFileCollectorNoDotLast(ANY_FILE_REGEX,
				mockFileCollectionInputAdapter);
		List<File> expectedFileList = createSortedExpectedFileList(testFileOne, testFileTwo, testFileThree);
		
		objUnderTest.run();
		verify(mockFileCollectionInputAdapter).sendEvent(argThat(contentsTheSame(expectedFileList)));
		
		final List<File> createdFiles = createFiles(folder, "testFileD.txt", "testFileE.txt");
		objUnderTest.run();
		expectedFileList = extendSortedFileListWithList(expectedFileList, createdFiles);
		verify(mockFileCollectionInputAdapter).sendEvent(argThat(contentsTheSame(expectedFileList)));
		
		final List<File> moreCreatedFiles = createFiles(folder, "testFileF.txt");
		objUnderTest.run();
		
		expectedFileList = extendSortedFileListWithList(expectedFileList, moreCreatedFiles);
		verify(mockFileCollectionInputAdapter).sendEvent(argThat(contentsTheSame(expectedFileList)));
	}
	
	@Test
	public void run_addDirectoriesAfterStartingThread_checkThatAllFilesAreProcessed()
			throws IOException, InterruptedException {
		when(mockFileCollectionInputAdapter.getConfiguredDirectoryList()).thenReturn(new ArrayList<File>());
		
		final ScheduledFileCollector objUnderTest = new ScheduledFileCollectorNoDotLast(ANY_FILE_REGEX,
				mockFileCollectionInputAdapter);
		final List<File> expectedFileList = createSortedExpectedFileList(testFileOne, testFileTwo, testFileThree);
		
		objUnderTest.run();
		
		verify(mockFileCollectionInputAdapter).getConfiguredDirectoryList();
		verifyNoMoreInteractions(mockFileCollectionInputAdapter);
		
		when(mockFileCollectionInputAdapter.getConfiguredDirectoryList()).thenReturn(directoryList);
		objUnderTest.run();
		
		verify(mockFileCollectionInputAdapter).sendEvent(argThat(contentsTheSame(expectedFileList)));
	}
	
	private List<File> createSortedExpectedFileList(final File... files) {
		final List<File> expectedFileList = new ArrayList<File>();
		return extendSortedExpectedFileList(expectedFileList, files);
	}
	
	private List<File> extendSortedExpectedFileList(final List<File> expectedFileList, final File... files) {
		for (final File file : files) {
			expectedFileList.add(file);
		}
		Collections.sort(expectedFileList);
		return expectedFileList;
	}
	
	private List<File> extendSortedFileListWithList(final List<File> expectedFileList, final List<File> files) {
		expectedFileList.addAll(files);
		Collections.sort(expectedFileList);
		return expectedFileList;
	}
	
	/**
	 * 
	 * @param folder
	 *            - parent folder of the files to create
	 * @param names
	 *            - names of the files to create
	 * @return list of the created Files
	 * @throws IOException
	 */
	private List<File> createFiles(final TemporaryFolder folder, final String... names) throws IOException {
		final List<File> createdFiles = new ArrayList<File>();
		for (final String name : names) {
			final File file = folder.newFile(name);
			createdFiles.add(file);
		}
		return createdFiles;
	}
}
