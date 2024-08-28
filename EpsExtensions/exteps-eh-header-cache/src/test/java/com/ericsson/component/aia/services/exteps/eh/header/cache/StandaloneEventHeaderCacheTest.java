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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.component.aia.mediation.parsers.streamrecord.StreamedRecord;

@RunWith(MockitoJUnitRunner.class)
public class StandaloneEventHeaderCacheTest extends EventHeaderCacheHandlerTestBase {

    @Test
    public void processing_an_event_message_without_connect_should_retrieve_connect_from_cache_before_processing_event() {

        final EventHeaderCacheHandler handler = spy(getHandler());

        handler.inputEvents(newConnect(SOURCE_ID));
        handler.inputEvents(newEvent(SOURCE_ID));

        final ArgumentCaptor<StreamedRecord> argument = ArgumentCaptor.forClass(StreamedRecord.class);

        verify(handler, times(2)).sendEvent(argument.capture());

        final List<StreamedRecord> records = argument.getAllValues();
        assertEquals(CONNECT, records.get(0).getAction());
        assertEquals(EVENT, records.get(1).getAction());
    }

    @Test
    public void processing_an_event_message_without_connect_and_with_remote_cache_should_resend_connect_header() {

        final EventHeaderCacheHandler handler = spy(getHandler());

        final StreamedRecord event = newEvent(SOURCE_ID);
        handler.getRemoteCache().put(SOURCE_ID, event);
        handler.inputEvents(event);

        verify(handler).resendHeader(event);
        verify(handler, times(2)).sendEvent(any(StreamedRecord.class));
        assertTrue(handler.getLocalCache().contains(SOURCE_ID));
    }

    @Test
    public void processing_an_event_message_without_connect_and_without_remote_cache_should_NOT_resend_connect_header() {

        final EventHeaderCacheHandler handler = spy(getHandler());

        final StreamedRecord event = newEvent(SOURCE_ID);
        handler.inputEvents(event);

        verify(handler).resendHeader(event);
        verify(handler, times(1)).sendEvent(event);
        assertFalse(handler.getLocalCache().contains(SOURCE_ID));
    }

    @Test
    public void processing_an_unknown_message_should_pass_it_to_the_next_handler() {
        final EventHeaderCacheHandler handler = spy(getHandler());

        final StreamedRecord unknownMessage = newUnknownMessage(SOURCE_ID);

        handler.inputEvents(unknownMessage);

        verify(handler, times(1)).sendEvent(unknownMessage);
    }

    @Test
    public void processing_a_disconnect_message_should_remove_from_cache_and_passed_to_next_handler() {
        final EventHeaderCacheHandler handler = spy(getHandler());

        final StreamedRecord disconnect = newDisconnect(SOURCE_ID);

        handler.inputEvents(disconnect);

        assertFalse(handler.getLocalCache().contains(SOURCE_ID));
        assertFalse(handler.getRemoteCache().containsKey(SOURCE_ID));
        verify(handler).sendEvent(disconnect);
    }
}
