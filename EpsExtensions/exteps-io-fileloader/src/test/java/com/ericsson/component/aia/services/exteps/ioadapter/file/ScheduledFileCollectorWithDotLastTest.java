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
import static com.ericsson.component.aia.services.exteps.ioadapter.file.utils.FileInputAdapterConfigParams.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
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
import com.ericsson.component.aia.services.exteps.ioadapter.file.ScheduledFileCollectorWithDotLastProcessing;

/**
 * This class is a test class for {@link ScheduledFileCollectorWithDotLastProcessing}
 * 
 * @since 0.0.1-SNAPSHOT
 */
@RunWith(MockitoJUnitRunner.class)
public class ScheduledFileCollectorWithDotLastTest {
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	
	@Mock
	private FileCollectionInputAdapter mockFileCollectionInputAdapter;
	
	private List<File> directoryList;
	
	private static final String LAST_LIST_EXTENSION = ".last";
	
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
	public void run_NoMatchingRegex_ShouldProcessNothing() {
		final ScheduledFileCollector objUndertest = new ScheduledFileCollectorWithDotLastProcessing(
				"\\.nonExistingExt^", DEFAULT_MAX_LAST_FILE_SIZE, DEFAULT_LAST_FILE_REDUCTION_PERCENTAGE,
				mockFileCollectionInputAdapter);
		
		objUndertest.run();
		
		verify(mockFileCollectionInputAdapter, atLeast(1)).getConfiguredDirectoryList();
		verifyNoMoreInteractions(mockFileCollectionInputAdapter);
	}
	
	@Test
	public void run_firstTimeWithHiddenFiles_ShouldProcessAllFilesExceptTheHiddenOnes() {
		final ScheduledFileCollector objUndertest = new ScheduledFileCollectorWithDotLastProcessing(ANY_FILE_REGEX,
				DEFAULT_MAX_LAST_FILE_SIZE, DEFAULT_LAST_FILE_REDUCTION_PERCENTAGE, mockFileCollectionInputAdapter);
		final List<File> expectedFileList = createSortedExpectedFileList(testFileOne, testFileTwo, testFileThree);
		
		objUndertest.run();
		
		verify(mockFileCollectionInputAdapter).sendEvent(argThat(contentsTheSame(expectedFileList)));
	}
	
	@Test
	public void run_secondRun_shouldNotProcessFilesAgainBecauseofDotLastFile() {
		final ScheduledFileCollector objUndertest = new ScheduledFileCollectorWithDotLastProcessing(ANY_FILE_REGEX,
				DEFAULT_MAX_LAST_FILE_SIZE, DEFAULT_LAST_FILE_REDUCTION_PERCENTAGE, mockFileCollectionInputAdapter);
		
		final List<File> expectedFileList = createSortedExpectedFileList(testFileOne, testFileTwo, testFileThree);
		objUndertest.run();
		
		verify(mockFileCollectionInputAdapter).sendEvent(argThat(contentsTheSame(expectedFileList)));
		objUndertest.run();
		verify(mockFileCollectionInputAdapter, atLeast(1)).getConfiguredDirectoryList();
		verifyNoMoreInteractions(mockFileCollectionInputAdapter);
		final File lastFile = new File(directoryList.get(0).getAbsolutePath() + File.separator + LAST_LIST_EXTENSION);
		
		assertTrue("Could not find .last file in " + directoryList.get(0).getAbsolutePath(), lastFile.exists());
	}
	
	@Test
	public void run_secondFileCollectorRun_shouldNotProcessSameFilesAgainBecauseofDotLastFile() {
		final ScheduledFileCollector objUndertest = new ScheduledFileCollectorWithDotLastProcessing(ANY_FILE_REGEX,
				DEFAULT_MAX_LAST_FILE_SIZE, DEFAULT_LAST_FILE_REDUCTION_PERCENTAGE, mockFileCollectionInputAdapter);
		
		final List<File> expectedFileList = createSortedExpectedFileList(testFileOne, testFileTwo, testFileThree);
		objUndertest.run();
		
		verify(mockFileCollectionInputAdapter).sendEvent(argThat(contentsTheSame(expectedFileList)));
		
		final ScheduledFileCollector objUndertest2 = new ScheduledFileCollectorWithDotLastProcessing(ANY_FILE_REGEX,
				DEFAULT_MAX_LAST_FILE_SIZE, DEFAULT_LAST_FILE_REDUCTION_PERCENTAGE, mockFileCollectionInputAdapter);
		objUndertest2.run();
		verify(mockFileCollectionInputAdapter, atLeast(1)).getConfiguredDirectoryList();
		verifyNoMoreInteractions(mockFileCollectionInputAdapter);
	}
	
	@Test
	public void run_checkLastFileContent_contentIsCorrect() throws IOException, InterruptedException {
		final ScheduledFileCollector objUnderTest = new ScheduledFileCollectorWithDotLastProcessing(ANY_FILE_REGEX,
				DEFAULT_MAX_LAST_FILE_SIZE, DEFAULT_LAST_FILE_REDUCTION_PERCENTAGE, mockFileCollectionInputAdapter);
		final List<File> expectedFileList = createSortedExpectedFileList(testFileOne, testFileTwo, testFileThree);
		
		objUnderTest.run();
		
		assertEquals(expectedFileList, getSortedContentsOfDotLastFile());
	}
	
	@Test
	public void run_addmoreFilesAfterFirstRun_checkThatTheyAreTheOnlyOnesProcessed()
			throws IOException, InterruptedException {
		final ScheduledFileCollector objUnderTest = new ScheduledFileCollectorWithDotLastProcessing(ANY_FILE_REGEX,
				DEFAULT_MAX_LAST_FILE_SIZE, DEFAULT_LAST_FILE_REDUCTION_PERCENTAGE, mockFileCollectionInputAdapter);
		final List<File> expectedFileList = createSortedExpectedFileList(testFileOne, testFileTwo, testFileThree);
		
		objUnderTest.run();
		verify(mockFileCollectionInputAdapter).sendEvent(argThat(contentsTheSame(expectedFileList)));
		
		final List<File> createdFiles = createFiles(folder, "testFileD.txt", "testFileE.txt");
		objUnderTest.run();
		verify(mockFileCollectionInputAdapter).sendEvent(argThat(contentsTheSame(createdFiles)));
		
		final List<File> moreCreatedFiles = createFiles(folder, "testFileF.txt");
		objUnderTest.run();
		verify(mockFileCollectionInputAdapter).sendEvent(argThat(contentsTheSame(moreCreatedFiles)));
	}
	
	@Test
	public void run_addmoreFilesAfterFirstRun_checkThatContentOfDotLastFileisCorrect()
			throws IOException, InterruptedException {
		final ScheduledFileCollector objUnderTest = new ScheduledFileCollectorWithDotLastProcessing(ANY_FILE_REGEX,
				DEFAULT_MAX_LAST_FILE_SIZE, DEFAULT_LAST_FILE_REDUCTION_PERCENTAGE, mockFileCollectionInputAdapter);
		List<File> expectedFileList = createSortedExpectedFileList(testFileOne, testFileTwo, testFileThree);
		objUnderTest.run();
		assertTrue(expectedFileList.containsAll(getSortedContentsOfDotLastFile()));
		
		final List<File> createdFiles = createFiles(folder, "testFileD.txt", "testFileE.txt");
		objUnderTest.run();
		expectedFileList = extendSortedFileListWithList(expectedFileList, createdFiles);
		assertTrue(expectedFileList.containsAll(getSortedContentsOfDotLastFile()));
		
		final List<File> moreCreatedFiles = createFiles(folder, "testFileF.txt");
		expectedFileList = extendSortedFileListWithList(expectedFileList, moreCreatedFiles);
		objUnderTest.run();
		assertEquals(expectedFileList, getSortedContentsOfDotLastFile());
	}
	
	@Test
	public void checkDotLastFileContentIsCorrectWithMultipleListingsWithDotLastFileExitsAndItsSizeReduces()
			throws IOException, InterruptedException {
		
		final ScheduledFileCollector objUnderTest = new ScheduledFileCollectorWithDotLastProcessing(ANY_FILE_REGEX, 3,
				50, mockFileCollectionInputAdapter);
		List<File> expectedFileList = createSortedExpectedFileList(testFileOne, testFileTwo, testFileThree);
		objUnderTest.run();
		List<File> unsortedContentsOfDotLastFile = getUnsortedContentsOfDotLastFile();
		assertThat(expectedFileList, contentsTheSame(unsortedContentsOfDotLastFile));
		verify(mockFileCollectionInputAdapter).sendEvent(argThat(contentsTheSame(expectedFileList)));
		final List<File> createdFiles = createFiles(folder, "testFileD.txt", "testFileE.txt");
		objUnderTest.run();
		expectedFileList = extendSortedFileListWithBeyondLimitDropOldFiles(unsortedContentsOfDotLastFile, createdFiles,
				3, 50);
		unsortedContentsOfDotLastFile = getSortedContentsOfDotLastFile();
		verify(mockFileCollectionInputAdapter).sendEvent(argThat(contentsTheSame(createdFiles)));
		
		assertEquals(unsortedContentsOfDotLastFile, expectedFileList);
	}
	
	private List<File> getSortedContentsOfDotLastFile() throws IOException {
		final List<File> fileList = getUnsortedContentsOfDotLastFile();
		Collections.sort(fileList);
		return fileList;
	}
	
	private List<File> getUnsortedContentsOfDotLastFile() throws IOException {
		final File dotLastFile = new File(
				directoryList.get(0).getAbsolutePath() + File.separator + LAST_LIST_EXTENSION);
		final List<File> fileList = readFilesFromDotLastFile(dotLastFile);
		return fileList;
	}
	
	private List<File> readFilesFromDotLastFile(final File dotLastFile) throws IOException {
		final BufferedReader bufferedReader = new BufferedReader(new FileReader(dotLastFile));
		String line;
		
		final List<File> actualFileList = new ArrayList<File>();
		while ((line = bufferedReader.readLine()) != null) {
			actualFileList.add(new File(line));
		}
		
		bufferedReader.close();
		
		return actualFileList;
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
	
	private List<File> extendSortedFileListWithBeyondLimitDropOldFiles(final List<File> expectedFileList,
			final List<File> files, final int max_Size, final int reductionPercentage) {
		
		final int combinedFileSize = expectedFileList.size() + files.size();
		if (combinedFileSize > max_Size) {
			final int reduceByPercent = combinedFileSize * reductionPercentage / 100;
			final Iterator<File> iter = expectedFileList.iterator();
			for (int i = 0; i < reduceByPercent; i++) {
				iter.next();
				iter.remove();
			}
		}
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
