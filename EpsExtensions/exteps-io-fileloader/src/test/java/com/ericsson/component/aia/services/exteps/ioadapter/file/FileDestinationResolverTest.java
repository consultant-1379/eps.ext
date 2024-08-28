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

import java.text.ParseException;

import org.junit.*;

import com.ericsson.component.aia.services.exteps.ioadapter.file.FileDestinationResolver;
import com.ericsson.component.aia.services.exteps.ioadapter.file.FileUtils;
import com.ericsson.component.aia.services.exteps.ioadapter.file.Key;
import com.ericsson.component.aia.itpf.common.event.handler.EventSubscriber;

/**
 * This class is a test class for {@link FileDestinationResolver}
 * 
 * @since 0.0.1-SNAPSHOT
 */
public class FileDestinationResolverTest {
	
	private static final int FIFTEEN_MINUTES = 900000;
	
	private EventSubscriber eventSubscriberA;
	private EventSubscriber eventSubscriberB;
	
	private EventSubscriber[] subscribers;
	private Key key;
	
	@Before
	public void setup() throws ParseException {
		
		subscribers = new EventSubscriber[] { eventSubscriberA, eventSubscriberB };
		
		final String fileName = "A20130902.0305-0310_SubNetwork=ONRM_ROOT_MO_R,MeContext=MAL05188_celltracefile_1-1378091475942.bin.gz";
		
		final String nodeName = FileUtils.getCellTraceNodeNameForKey(fileName);
		final long ropTime = FileUtils.nameToCalendar(fileName).getTimeInMillis() / FIFTEEN_MINUTES * FIFTEEN_MINUTES;
		
		key = new Key(nodeName, ropTime);
	}
	
	@Test
	public void resolveDestination_subscriberReturned_correctSubscriberChosen() {
		
		final EventSubscriber subscriber = FileDestinationResolver.resolveDestination(key, subscribers);
		
		int arrayPosition = key.getNodeName().hashCode() % subscribers.length;
		
		if (arrayPosition < 0) {
			arrayPosition *= -1;
		}
		
		Assert.assertEquals(subscriber, subscribers[arrayPosition]);
	}
	
}
