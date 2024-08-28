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

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import com.ericsson.component.aia.services.exteps.ioadapter.file.FileRopSorterEventHandler;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.component.aia.itpf.common.event.handler.EventSubscriber;

/**
 * This class is a test class for {@link FileRopSorterEventHandler}
 * 
 * @since 0.0.1-SNAPSHOT
 */
public class FileRopSorterEventHandlerTest {
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	private File testFileOne;
	
	private FileRopSorterEventHandler fileDistributorEventHandler;
	private List<EventSubscriber> mockedSubscriberList;
	
	@Before
	public void setup() throws IOException {
		
		testFileOne = folder.newFile(
				"A20130902.0300-0305_SubNetwork=ONRM_ROOT_MO_R,MeContext=CTL01014_celltracefile_1-1378091173826.bin.gz");
		
		final CreateContextForTest ccft = new CreateContextForTest();
		
		mockedSubscriberList = ccft.setUpSubscribers(1);
		
		final EventHandlerContext ctx = ccft.createContext(mockedSubscriberList);
		
		fileDistributorEventHandler = new FileRopSorterEventHandler();
		fileDistributorEventHandler.init(ctx);
	}
	
	@Test
	public void sendEvent_verifySendEvent_sendEventWorks() {
		
		final Object object = new Object();
		
		fileDistributorEventHandler.sendEvent(object);
		
		verify(mockedSubscriberList.get(0)).sendEvent(object);
	}
	
	@Test
	public void onEvent_verifyOnEvent_sendEventOnSubscriberCalled() {
		
		final List<File> fileList = new ArrayList<File>();
		fileList.add(testFileOne);
		
		fileDistributorEventHandler.onEvent(fileList);
		
		verify(mockedSubscriberList.get(0)).sendEvent(any(Map.class));
	}
	
	@After
	public void cleanup() {
		folder.delete();
	}
}
