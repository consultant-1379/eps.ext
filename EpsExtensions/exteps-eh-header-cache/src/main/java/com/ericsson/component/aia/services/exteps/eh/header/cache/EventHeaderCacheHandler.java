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
import static com.ericsson.component.aia.services.exteps.eh.header.cache.CacheUtils.*;

import java.util.Set;

import javax.cache.Cache;

import com.ericsson.component.aia.mediation.parsers.streamrecord.StreamedRecord;
import com.ericsson.component.aia.services.exteps.eh.header.AbstractHeaderHandler;
import com.ericsson.component.aia.services.exteps.io.common.statistics.EpsExtStatisticsHelper;
import com.ericsson.oss.itpf.modeling.annotation.cache.CacheMode;
import com.ericsson.oss.itpf.sdk.cache.classic.CacheProviderBean;

/**
 * Handler responsible for caching the CONNECT message between instances of flows.
 */
public class EventHeaderCacheHandler extends AbstractHeaderHandler {

    private Cache<Integer, StreamedRecord> remoteCache;

    private EpsExtStatisticsHelper epsExtStatisticsHelper;

    @Override
    public boolean understandsURI(final String uri) {
        return true;
    }

    @Override
    public void destroyAll() {

    }

    /**
     * Invoked only once during initialization but before any event processing.
     */
    @Override
    protected void doInit() {
        log.info("Initialising Cache ...");

        final String cacheName = getStringProperty(CACHE_NAME.getKey());
        final CacheMode cacheMode = CacheMode.valueOf(getStringProperty(CACHE_MODE.getKey()));
        final int replicas = Integer.parseInt(getStringProperty(CACHE_REPLICAS.getKey()));
        final int timeToLive = Integer.parseInt(getStringProperty(TIME_TO_LIVE.getKey()));
        final String maxEntries = getStringProperty(MAX_ENTRIES.getKey());

        remoteCache = new CacheProviderBean().createOrGetCache(cacheName, getCacheConfiguration(cacheMode, replicas, timeToLive, maxEntries));
        epsExtStatisticsHelper = new EpsExtStatisticsHelper(this.getClass().getSimpleName());
        epsExtStatisticsHelper.initialiseStatistics(getEventHandlerContext());
    }

    @Override
    protected void handleConnectionEvent(final StreamedRecord connectRecord) {
        final int sourceId = connectRecord.getSourceId();
        remoteCache.put(sourceId, connectRecord);
    }

    @Override
    protected void handleDisconnectEvent(final StreamedRecord disconnectRecord) {
        final int sourceId = disconnectRecord.getSourceId();
        if (!remoteCache.remove(sourceId)) {
            log.debug("CONNECT header doesn't exist in remote cache for: sourceId={}", sourceId);
        }
    }

    /**
     * Sends specified event to all subscribers.
     *
     * @param inputEvent
     *            the event to send to subscribers
     */
    @Override
    public void sendEvent(final Object inputEvent) {
        sendToAllSubscribers(inputEvent);
        if (epsExtStatisticsHelper.isStatisticsOn()) {
            epsExtStatisticsHelper.mark();
        }
    }

    /**
     * Resend CONNECT header if not present in the local cache.
     *
     * @param record
     *            Stream input record.
     */
    @Override
    protected void resendHeader(final StreamedRecord record) {
        final int sourceId = record.getSourceId();
        if (remoteCache.containsKey(sourceId)) {
            sendEvent(remoteCache.get(sourceId));
            localCache.add(sourceId);
        } else {
            log.debug("CONNECT header doesn't exist in distributed cache for: sourceId={}", sourceId);
        }
    }

    /**
     * Retrieve the local cache.
     *
     * @return The local cache.
     */
    Set<Integer> getLocalCache() {
        return localCache;
    }

    /**
     * Retrieve the remote cache.
     *
     * @return The remote cache.
     */
    Cache<Integer, StreamedRecord> getRemoteCache() {
        return remoteCache;
    }

}
