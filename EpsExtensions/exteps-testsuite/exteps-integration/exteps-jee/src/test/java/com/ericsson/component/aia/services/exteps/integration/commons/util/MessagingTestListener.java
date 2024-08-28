/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.exteps.integration.commons.util;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public abstract class MessagingTestListener {

	public CountDownLatch latch;

	private final List<Object> receivedMessages = new LinkedList<>();

	void initCountDown(final int count) {
		latch = new CountDownLatch(count);
	}

	public void mark(final Object msg) {
		receivedMessages.add(msg);
		latch.countDown();
	}

	public List<Object> getReceivedMessages() {
		return receivedMessages;
	}

	public void clear(final int count) {
		receivedMessages.clear();
		latch = new CountDownLatch(count);
	}
	
}
