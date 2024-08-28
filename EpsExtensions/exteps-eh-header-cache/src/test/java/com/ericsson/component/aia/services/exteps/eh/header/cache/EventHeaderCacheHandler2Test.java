/*
 * ------------------------------------------------------------------------------
 * *****************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 * *****************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.component.aia.services.exteps.eh.header.cache;

import static com.ericsson.component.aia.mediation.parsers.streamrecord.StreamedRecord.Actions.CONNECT;
import static com.ericsson.component.aia.mediation.parsers.streamrecord.StreamedRecord.Actions.EVENT;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.mediation.parsers.streamrecord.StreamedRecord;

/**
 * This class simulates the second parser node that should take over the first parser node in case of failure of it.
 * Processing an EVENT message without the corresponding CONNECT message will cause it to retrieve the CONNECT message
 * from the distributed cache.
 */
@RunWith(MockitoJUnitRunner.class)
@Ignore("Disabled when running on Jenking because of the timming coordination (sleep)")
public class EventHeaderCacheHandler2Test extends EventHeaderCacheHandlerTestBase {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Test
    public void processing_an_event_message_without_connect_should_retrieve_connect_from_cache_before_processing_event() {

        log.info("Waiting 30 seconds for the other node...");
        sleepSeconds(30);
        final EventHeaderCacheHandler handler = spy(getHandler());
        log.info("Waiting more 20 seconds for CONNECT message to arrive...");
        sleepSeconds(20);

        handler.inputEvents(newEvent(SOURCE_ID));

        final ArgumentCaptor<StreamedRecord> argument = ArgumentCaptor.forClass(StreamedRecord.class);

        verify(handler, times(2)).sendEvent(argument.capture());

        final List<StreamedRecord> records = argument.getAllValues();
        assertEquals(CONNECT, records.get(0).getAction());
        assertEquals(EVENT, records.get(1).getAction());
    }
}
