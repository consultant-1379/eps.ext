/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.component.aia.epscomponent;

import static org.junit.Assert.*;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import com.ericsson.component.aia.itpf.common.event.handler.*;

public class SingleThreadQueueHandlerTest {
    private StubbedEventSubscriber subscriber;
    private SingleThreadQueueHandler objUnderTest;
    private StubbedEpsStatisticsRegister epsStatisticsRegister;

    @Before
    public void setup() {
        subscriber = new StubbedEventSubscriber();
        final StubbedConfiguration configuration = new StubbedConfiguration();
        configuration.setProperty("threadqueuesize", "1");
        configuration.setProperty("threadname", "testThread");
        epsStatisticsRegister = new StubbedEpsStatisticsRegister();
        final StubbedContext context = new StubbedContext(configuration, subscriber, epsStatisticsRegister);
        objUnderTest = new SingleThreadQueueHandler();
        objUnderTest.init(context);
    }

    @Test
    public void onEvent_withValidEvents_shouldBeAddedToBuffer() throws Exception {
        EventObject eventObject = new EventObject();
        objUnderTest.onEvent(eventObject);
        waitForEventToBeReceived();
        assertEquals(eventObject, subscriber.getReceivedObject());
    }

    @Test
    public void onEvent_withMoreValidEventsThanBuffer_EventsShouldBeDropped() throws Exception {
        EventObject eventObject = new EventObject();
        for (int x = 0; x < 100; x++) {
            objUnderTest.onEvent(eventObject);
        }
        waitForEventToBeReceived();
        assertEquals(eventObject, subscriber.getReceivedObject());
        assertTrue("Did not get any dropped events", epsStatisticsRegister.getCounter("apeps.testThreadThreadQueueDroppedRecords").getCount() > 1);
    }

    @Test
    public void destroy_shouldStopProcessEvents() throws Exception {
        final StubbedConfiguration config = new StubbedConfiguration();
        config.setProperty("udpMultiCastAddress", "228.7.8.1");
        config.setProperty("threadqueuesize", "50000");
        config.setProperty("threadname", "test");
        final StubbedContext context = new StubbedContext(config, subscriber, epsStatisticsRegister);
        final StubbedSingleThreadQueueHandler singleThreadQueueHandler = new StubbedSingleThreadQueueHandler();
        singleThreadQueueHandler.init(context);
        final Thread thread = new Thread() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    singleThreadQueueHandler.onEvent("event");
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException exception) {
                        exception.printStackTrace();
                    }
                }
            }
        };
        thread.start();
        verifyCallsToSendEvent(singleThreadQueueHandler);
        singleThreadQueueHandler.destroy();
        verifyNoCallsToSendEvent(singleThreadQueueHandler);
        thread.interrupt();
    }

    private void verifyCallsToSendEvent(final StubbedSingleThreadQueueHandler singleThreadQueueHandler) throws Exception {
        final long numOfCallsToSendEvent = singleThreadQueueHandler.getNumOfCallsToSendEvent();
        Thread.sleep(100);
        String msg = String.format("Number of calls to sendEvent should keep increasing expected %s greater than %s"
                , singleThreadQueueHandler.getNumOfCallsToSendEvent(), numOfCallsToSendEvent);

        assertTrue(msg, singleThreadQueueHandler.getNumOfCallsToSendEvent() > numOfCallsToSendEvent);
    }

    private void verifyNoCallsToSendEvent(final StubbedSingleThreadQueueHandler singleThreadQueueHandler) throws Exception {
        Thread.sleep(100);
        final long numOfCallsToSendEvent = singleThreadQueueHandler.getNumOfCallsToSendEvent();
        Thread.sleep(100);
        assertEquals("Calls to sendEvent should have stopped after destroy", singleThreadQueueHandler.getNumOfCallsToSendEvent(),
                numOfCallsToSendEvent);
    }

    private void waitForEventToBeReceived() throws Exception {
        int count = 0;
        while (!subscriber.isReceived() && count++ < 10) {
            TimeUnit.SECONDS.sleep(1);
        }
    }

    private class StubbedSingleThreadQueueHandler extends SingleThreadQueueHandler {
        private long numOfCallsToSendEvent = 0;

        @Override
        public void sendEvent(final Object object) {
            numOfCallsToSendEvent++;
        }

        long getNumOfCallsToSendEvent() {
            return numOfCallsToSendEvent;
        }

    }

    private class StubbedEventSubscriber implements EventSubscriber {

        private Object receivedObject;

        private boolean received;

        @Override
        public void sendEvent(final Object event) {
            receivedObject = event;
            received = true;
        }

        @Override
        public String getIdentifier() {
            return "StubbedEventSubscriber";
        }

        Object getReceivedObject() {
            return receivedObject;
        }

        public boolean isReceived() {
            return received;
        }
    }

    private class EventObject {
        private final UUID identifier;

        EventObject() {
            identifier = UUID.randomUUID();
        }

        @Override
        public boolean equals(final Object other) {
            if (this == other) {
                return true;
            }
            if (other == null || getClass() != other.getClass()) {
                return false;
            }

            final EventObject that = (EventObject) other;

            return identifier != null ? identifier.equals(that.identifier) : that.identifier == null;

        }

        @Override
        public int hashCode() {
            return identifier != null ? identifier.hashCode() : 0;
        }
    }
}