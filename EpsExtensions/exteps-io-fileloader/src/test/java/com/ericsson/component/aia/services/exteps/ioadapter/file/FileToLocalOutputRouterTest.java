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

import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

import org.junit.*;
import org.junit.rules.TemporaryFolder;

import com.ericsson.component.aia.services.exteps.ioadapter.file.FileToLocalOutputRouter;
import com.ericsson.component.aia.services.exteps.ioadapter.file.FileUtils;
import com.ericsson.component.aia.services.exteps.ioadapter.file.Key;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.component.aia.itpf.common.event.handler.EventSubscriber;

/**
 * This class is a test class for {@link FileToLocalOutputRouter}
 * 
 * @since 0.0.1-SNAPSHOT
 */
public class FileToLocalOutputRouterTest {
	
	private static final int FIFTEEN_MINUTES = 900000;
	
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	
	private static final int NUM_OF_SUBSCRIBERS = 3;
	private File testFileOne;
	private FileToLocalOutputRouter fileToLocal;
	
	private Key key;
	
	List<EventSubscriber> mockedSubscriberList;
	Map<Key, List<File>> fileMap;
	
	@Before
	public void setup() throws IOException, ParseException {
		
		fileToLocal = new FileToLocalOutputRouter();
		
		final CreateContextForTest ccft = new CreateContextForTest();
		mockedSubscriberList = ccft.setUpSubscribers(NUM_OF_SUBSCRIBERS);
		final EventHandlerContext ctx = ccft.createContext(mockedSubscriberList);
		fileToLocal.init(ctx);
		
		fileMap = new HashMap<Key, List<File>>();
		
		testFileOne = folder.newFile(
				"A20130902.0300-0305_SubNetwork=ONRM_ROOT_MO_R,MeContext=CTL01014_celltracefile_1-1378091173826.bin.gz");
		
		final List<File> fileList = new ArrayList<File>();
		fileList.add(testFileOne);
		
		final String nodeName = FileUtils.getCellTraceNodeNameForKey(testFileOne.getName());
		final long ropTime = FileUtils.nameToCalendar(testFileOne.getName()).getTimeInMillis() / FIFTEEN_MINUTES
				* FIFTEEN_MINUTES;
		
		key = new Key(nodeName, ropTime);
		
		fileMap.put(key, fileList);
		
	}
	
	@Test
	public void onEvent_verifyOnEvent_subscriberAtIndex0Called() {
		
		fileToLocal.onEvent(fileMap);
		
		verify(mockedSubscriberList.get(0)).sendEvent(fileMap.get(key));
	}
	
	@Test
	public void onEvent_verifyOnEvent_notCorrectSubscriberCalled() {
		
		fileToLocal.onEvent(fileMap);
		
		verify(mockedSubscriberList.get(1), never()).sendEvent(fileMap.get(key));
		verify(mockedSubscriberList.get(2), never()).sendEvent(fileMap.get(key));
	}
	
	@After
	public void cleanup() {
		folder.delete();
	}
}
