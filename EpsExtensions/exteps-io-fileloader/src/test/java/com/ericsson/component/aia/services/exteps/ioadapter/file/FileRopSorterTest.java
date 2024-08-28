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
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

import junit.framework.Assert;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import com.ericsson.component.aia.services.exteps.ioadapter.file.FileRopSorter;
import com.ericsson.component.aia.services.exteps.ioadapter.file.FileUtils;
import com.ericsson.component.aia.services.exteps.ioadapter.file.Key;

/**
 * This class is a test class for {@link FileRopSorter}
 * 
 * @since 0.0.1-SNAPSHOT
 */
public class FileRopSorterTest {
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	
	private FileRopSorter fileProcessor;
	
	private File testFileTwoNodeName1;
	private File testFileThreeNodeName2;
	private File testFileOneROP1;
	private File testFileFourROP2;
	private File badTestFile;
	
	private static final int FIFTEEN_MINUTES = 900000;
	
	@Before
	public void setup() throws IOException {
		
		testFileOneROP1 = folder.newFile(
				"A20130902.0300-0315_SubNetwork=ONRM_ROOT_MO_R,MeContext=CTL01014_celltracefile_1-1378091173826.bin.gz");
		testFileFourROP2 = folder.newFile(
				"A20130902.0600-0615_SubNetwork=ONRM_ROOT_MO_R,MeContext=CTL01014_celltracefile_3-1378091804741.bin.gz");
		
		testFileTwoNodeName1 = folder.newFile(
				"A20130902.0310-0315_SubNetwork=ONRM_ROOT_MO_R,MeContext=MEL05018_celltracefile_3-1378091804741.bin.gz");
		testFileThreeNodeName2 = folder.newFile(
				"A20130902.0305-0310_SubNetwork=ONRM_ROOT_MO_R,MeContext=CTL01014_celltracefile_3-1378091508165.bin.gz");
		
		badTestFile = folder.newFile("badTestFile.bin.gz");
		
		fileProcessor = new FileRopSorter();
	}
	
	private Key getKey(final File file) throws ParseException {
		
		final String nodeName = FileUtils.getCellTraceNodeNameForKey(file.getName());
		final long ropTime = FileUtils.nameToCalendar(file.getName()).getTimeInMillis() / FIFTEEN_MINUTES
				* FIFTEEN_MINUTES;
		
		final Key key = new Key(nodeName, ropTime);
		
		return key;
	}
	
	@Test
	public void getFilesByROP_fileListProcessedPerROP_sendEventCalled() throws ParseException {
		
		final List<File> processedFileList = new ArrayList<File>();
		
		final List<File> fileListROP1 = new ArrayList<File>();
		final List<File> fileListROP2 = new ArrayList<File>();
		
		processedFileList.add(testFileOneROP1);
		processedFileList.add(testFileFourROP2);
		
		fileListROP1.add(testFileOneROP1);
		fileListROP2.add(testFileFourROP2);
		
		final Map<Key, ArrayList<File>> fileKeyMapActual = fileProcessor.sendFilesByROP(processedFileList);
		
		final Key keyROP1 = getKey(testFileOneROP1);
		final Key keyROP2 = getKey(testFileFourROP2);
		
		final HashMap<Key, List<File>> fileKeyMapExpected = new HashMap<Key, List<File>>();
		fileKeyMapExpected.put(keyROP1, fileListROP1);
		fileKeyMapExpected.put(keyROP2, fileListROP2);
		
		Assert.assertEquals(fileKeyMapExpected, fileKeyMapActual);
	}
	
	@Test
	public void getFilesByROP_fileListProcessedPerNodeName_sendEventCalled() throws ParseException {
		
		final List<File> processedFileList = new ArrayList<File>();
		
		final List<File> fileListNodeName1 = new ArrayList<File>();
		final List<File> fileListNodeName2 = new ArrayList<File>();
		
		processedFileList.add(testFileTwoNodeName1);
		processedFileList.add(testFileThreeNodeName2);
		
		fileListNodeName1.add(testFileTwoNodeName1);
		fileListNodeName2.add(testFileThreeNodeName2);
		
		final Map<Key, ArrayList<File>> fileKeyMapActual = fileProcessor.sendFilesByROP(processedFileList);
		
		final Key keyROP1 = getKey(testFileTwoNodeName1);
		final Key keyROP2 = getKey(testFileThreeNodeName2);
		
		final HashMap<Key, List<File>> fileKeyMapExpected = new HashMap<Key, List<File>>();
		fileKeyMapExpected.put(keyROP1, fileListNodeName1);
		fileKeyMapExpected.put(keyROP2, fileListNodeName2);
		
		Assert.assertEquals(fileKeyMapExpected, fileKeyMapActual);
	}
	
	@Test
	public void getFilesByROP_fileListProcessedWithBadFile_ExceptionThrown() throws ParseException {
		
		thrown.expect(ParseException.class);
		
		final List<File> processedFileList = new ArrayList<File>();
		
		processedFileList.add(testFileFourROP2);
		processedFileList.add(badTestFile);
		
		fileProcessor.sendFilesByROP(processedFileList);
	}
	
	@After
	public void cleanup() {
		folder.delete();
	}
}
