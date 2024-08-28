/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.component.aia.services.exteps.eh.router;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.ericsson.component.aia.services.exteps.eh.router.StreamedRecordRouter;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.component.aia.itpf.common.event.handler.EventSubscriber;
import com.ericsson.component.aia.mediation.parsers.streamrecord.StreamedRecord;

/**
 * This class is a test class for {@link StreamedRecordRouter}
 * 
 */
public class StreamedRecordRouterTest {

    private static final int NUM_OF_SUBSCRIBERS = 3;
    private StreamedRecordRouter streamedRecordRouter;

    List<EventSubscriber> mockedSubscriberList;

    CreateContextForTest ccft;

    @Before
    public void setup() throws IOException, ParseException {
        streamedRecordRouter = new StreamedRecordRouter();
        ccft = new CreateContextForTest();
    }

    @Test
    public void init_NullNumberOfSubscribers_noExceptionThrown() {
        mockedSubscriberList = ccft.setUpSubscribers(0);
        final EventHandlerContext ctx = ccft.createContext(mockedSubscriberList);
        streamedRecordRouter.init(ctx);
    }

    @Test
    public void onEvent_NoInstanceOfStreamRecord_NoAction() {
        mockedSubscriberList = ccft.setUpSubscribers(NUM_OF_SUBSCRIBERS);
        final EventHandlerContext ctx = ccft.createContext(mockedSubscriberList);
        streamedRecordRouter.init(ctx);

        final Object noStreamRecord = "NoStreamRecord";

        streamedRecordRouter.onEvent(noStreamRecord);

        verify(mockedSubscriberList.get(0), never()).sendEvent(noStreamRecord);
    }

    @Test
    public void onEvent_RouteIsNegative_NoAction() {

        mockedSubscriberList = ccft.setUpSubscribers(NUM_OF_SUBSCRIBERS);
        final EventHandlerContext ctx = ccft.createContext(mockedSubscriberList);
        streamedRecordRouter.init(ctx);

        final int SOURCE_ID = -4;

        final StreamedRecord record = new StreamedRecord(SOURCE_ID);
        for (int i = 0; i < NUM_OF_SUBSCRIBERS; i++) {
            verify(mockedSubscriberList.get(i), never()).sendEvent(record);
        }

        streamedRecordRouter.onEvent(record);
    }

    @Test
    public void onEvent_verifyOnEvent_subscriberAtIndex0Called() {

        mockedSubscriberList = ccft.setUpSubscribers(NUM_OF_SUBSCRIBERS);
        final EventHandlerContext ctx = ccft.createContext(mockedSubscriberList);
        streamedRecordRouter.init(ctx);

        final StreamedRecord record1 = new StreamedRecord(3);
        streamedRecordRouter.onEvent(record1);

        verify(mockedSubscriberList.get(0)).sendEvent(record1);
    }

    @Test
    public void onEvent_verifyOnEvent_subscriberAtIndex1Called() {

        mockedSubscriberList = ccft.setUpSubscribers(NUM_OF_SUBSCRIBERS);
        final EventHandlerContext ctx = ccft.createContext(mockedSubscriberList);
        streamedRecordRouter.init(ctx);

        final StreamedRecord record2 = new StreamedRecord(1);
        streamedRecordRouter.onEvent(record2);

        verify(mockedSubscriberList.get(1)).sendEvent(record2);
    }

    @Test
    public void onEvent_NoSubscribers_NoAction() {
        mockedSubscriberList = ccft.setUpSubscribers(0);
        final EventHandlerContext ctx = ccft.createContext(mockedSubscriberList);
        streamedRecordRouter.init(ctx);
        final StreamedRecord record1 = new StreamedRecord(3);
        streamedRecordRouter.onEvent(record1);

    }
}
