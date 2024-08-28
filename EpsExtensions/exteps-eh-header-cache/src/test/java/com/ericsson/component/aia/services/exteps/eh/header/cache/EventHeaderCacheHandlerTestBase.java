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

import static com.ericsson.component.aia.services.exteps.eh.header.cache.CacheProperties.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;

import com.ericsson.component.aia.itpf.common.config.Configuration;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.component.aia.mediation.parsers.streamrecord.StreamedRecord;
import com.ericsson.component.aia.services.eps.coordination.EpsAdaptiveConfiguration;

/**
 * Base class for testing {@link EventHeaderCacheHandler}.
 */
public class EventHeaderCacheHandlerTestBase {

    public static final int SOURCE_ID = 1;

    private EventHeaderCacheHandler handler;

    static void sleepSeconds(final int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            // Ignore.
        }
    }

    @Before
    public void init() {
        handler = new EventHeaderCacheHandler();
        handler.init(getContext());
        handler.doInit();
    }

    private EventHandlerContext getContext() {
        return new StubbedContext(getConfiguration());
    }

    private Configuration getConfiguration() {
        final Map<String, Object> configMap = new HashMap<>();
        configMap.put(CACHE_NAME.getKey(), "EventHeaderCache");
        configMap.put(CACHE_MODE.getKey(), "DISTRIBUTED_ASYNC");
        configMap.put(CACHE_REPLICAS.getKey(), "2");
        configMap.put(TIME_TO_LIVE.getKey(), "-1");
        configMap.put(MAX_ENTRIES.getKey(), "100000");
        final EpsAdaptiveConfiguration epsConfig = new EpsAdaptiveConfiguration();
        epsConfig.setConfiguration(configMap);
        return epsConfig;
    }

    EventHeaderCacheHandler getHandler() {
        return handler;
    }

    StreamedRecord newConnect(final int sourceId) {
        final StreamedRecord connect = new StreamedRecord(sourceId);
        connect.setAction(StreamedRecord.Actions.CONNECT);
        connect.setData(new byte[] {});
        connect.setRemoteIP(new byte[] {});
        return connect;
    }

    StreamedRecord newDisconnect(final int sourceId) {
        final StreamedRecord disconnect = new StreamedRecord(sourceId);
        disconnect.setAction(StreamedRecord.Actions.DISCONNECT);
        disconnect.setData(new byte[] {});
        disconnect.setRemoteIP(new byte[] {});
        return disconnect;
    }

    StreamedRecord newUnknownMessage(final int sourceId) {
        final StreamedRecord unknown = new StreamedRecord(sourceId);
        unknown.setAction(StreamedRecord.Actions.UNSET);
        unknown.setData(new byte[] {});
        unknown.setRemoteIP(new byte[] {});
        return unknown;
    }

    StreamedRecord newEvent(final int sourceId) {
        final StreamedRecord event = new StreamedRecord(sourceId);
        event.setAction(StreamedRecord.Actions.EVENT);
        event.setData(new byte[] {});
        event.setRemoteIP(new byte[] {});
        return event;
    }
}