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

import static com.ericsson.component.aia.services.exteps.ioadapter.file.utils.FileInputAdapterConfigParams.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.exteps.ioadapter.file.DirectoryNotFoundException;
import com.ericsson.component.aia.services.exteps.ioadapter.file.FileCollectionInputAdapter;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;

/**
 * This class is a test class for {@link FileCollectionInputAdapter}
 * 
 * @since 0.0.1-SNAPSHOT
 */
public class FileCollectionInputAdapterTest {
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	private FileCollectionInputAdapter fileCollectionInputAdapter;
	
	private int initialDelay;
	
	private int intervalValue;
	
	private String parentPath;
	
	private String directoryList;
	
	private File testFolder1;
	
	private File testFolder2;
	
	private File testFolder3;
	
	private File testFolder4;
	
	private static final String VALID_XML_FILE_REGEX = ".*_SubNetwork.*_MeContext.*.xml$";
	
	private EventHandlerContext eventHandlerContext;
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Before
	public void setup() {
		try {
			testFolder1 = folder.newFolder("dir1");
			testFolder2 = folder.newFolder("dir2");
			testFolder3 = folder.newFolder("dir3");
			testFolder4 = folder.newFolder("dir4");
		} catch (IOException e) {
			logger.error("Failed to create directory", e);
		}
		
		initialDelay = 1;
		intervalValue = 5;
		
		parentPath = folder.getRoot().getAbsolutePath();
		
		directoryList = testFolder1.getName() + "," + testFolder2.getName() + "," + testFolder3.getName() + ","
				+ testFolder4.getName();
		eventHandlerContext = new FileCollectionEventHandlerContextTest(parentPath, directoryList, initialDelay,
				intervalValue);
		fileCollectionInputAdapter = new FileCollectionInputAdapter();
		fileCollectionInputAdapter.init(eventHandlerContext);
	}
	
	@Test
	public void understandsURI_String_true() {
		assertTrue(fileCollectionInputAdapter.understandsURI(URI));
	}
	
	@Test
	public void understandsURI_wrongValue_false() {
		assertFalse(fileCollectionInputAdapter.understandsURI(""));
	}
	
	@Test
	public void understandsURI_nullValue_false() {
		assertFalse(fileCollectionInputAdapter.understandsURI(null));
	}
	
	@Test
	public void onEvent_null_ExceptionThrown() {
		thrown.expect(UnsupportedOperationException.class);
		fileCollectionInputAdapter.onEvent(null);
	}
	
	@Test
	public void getConfiguredDirectoryList_parentAndOneDirectoryAdded_Calculated()
			throws FileNotFoundException, DirectoryNotFoundException {
		eventHandlerContext = new FileCollectionEventHandlerContextTest(parentPath, testFolder1.getName(), initialDelay,
				intervalValue);
		final FileCollectionInputAdapter fileCollectionInputAdapter = new FileCollectionInputAdapter();
		fileCollectionInputAdapter.init(eventHandlerContext);
		final List<File> directoryListArray = fileCollectionInputAdapter.getConfiguredDirectoryList();
		final List<File> expected = new ArrayList<>();
		expected.add(testFolder1);
		assertEquals(expected, directoryListArray);
	}
	
	@Test
	public void getConfiguredDirectoryList_parentAndMultipleDirectorysAdded_Calculated()
			throws FileNotFoundException, DirectoryNotFoundException {
		final List<File> directoryListArray = fileCollectionInputAdapter.getConfiguredDirectoryList();
		final List<File> expected = new ArrayList<>();
		expected.add(testFolder1);
		expected.add(testFolder2);
		expected.add(testFolder3);
		expected.add(testFolder4);
		assertEquals(expected, directoryListArray);
	}
	
	@Test
	public void getConfiguredDirectoryList_nullParentAndDirectorys_shouldGetEmptyArray()
			throws FileNotFoundException, DirectoryNotFoundException {
		final String parentDir = null;
		eventHandlerContext = new FileCollectionEventHandlerContextTest(parentDir, testFolder1.getName(), initialDelay,
				intervalValue);
		fileCollectionInputAdapter = new FileCollectionInputAdapter();
		fileCollectionInputAdapter.init(eventHandlerContext);
		final List<File> directoryListArray = fileCollectionInputAdapter.getConfiguredDirectoryList();
		
		assertEquals(new ArrayList<File>(), directoryListArray);
	}
	
	@Test
	public void testgetRegexToUseWithInValidregex() {
		final FileCollectionInputAdapter adapter = new FileCollectionInputAdapter();
		
		assertEquals(ANY_FILE_REGEX, adapter.getRegexToUse("?abc"));
	}
	
	@Test
	public void testgetRegexToUseWithValidregex() {
		final FileCollectionInputAdapter adapter = new FileCollectionInputAdapter();
		
		assertEquals(VALID_XML_FILE_REGEX, adapter.getRegexToUse(VALID_XML_FILE_REGEX));
	}
	
	@Test
	public void testgetReductionPercentageoUseWithValidPercentage() {
		final FileCollectionInputAdapter adapter = new FileCollectionInputAdapter();
		
		assertEquals(10, adapter.getPercentageToUse(10));
	}
	
	@Test
	public void testgetReductionPercentageoUseWithInValidNegativePercentage() {
		final FileCollectionInputAdapter adapter = new FileCollectionInputAdapter();
		
		assertEquals(DEFAULT_LAST_FILE_REDUCTION_PERCENTAGE, adapter.getPercentageToUse(-1000));
	}
	
	@Test
	public void testgetReductionPercentageoUseWithInValidPositivePercentage() {
		final FileCollectionInputAdapter adapter = new FileCollectionInputAdapter();
		
		assertEquals(DEFAULT_LAST_FILE_REDUCTION_PERCENTAGE, adapter.getPercentageToUse(1000));
	}
	
	@After
	public void cleanup() {
		folder.delete();
	}
	
}
