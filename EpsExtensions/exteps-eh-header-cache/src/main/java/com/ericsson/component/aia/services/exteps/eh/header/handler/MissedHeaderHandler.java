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

package com.ericsson.component.aia.services.exteps.eh.header.handler;

import static com.ericsson.component.aia.services.exteps.eh.header.cache.CacheProperties.CACHE_MODE;
import static com.ericsson.component.aia.services.exteps.eh.header.cache.CacheProperties.CACHE_NAME;
import static com.ericsson.component.aia.services.exteps.eh.header.cache.CacheProperties.CACHE_REPLICAS;
import static com.ericsson.component.aia.services.exteps.eh.header.cache.CacheProperties.MAX_ENTRIES;
import static com.ericsson.component.aia.services.exteps.eh.header.cache.CacheProperties.TIME_TO_LIVE;

import javax.cache.Cache;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import com.ericsson.component.aia.mediation.netty.protocol.parser.DefaultStreamOutProtocolParser;
import com.ericsson.component.aia.mediation.netty.protocol.parser.MuxConnection;
import com.ericsson.component.aia.mediation.parsers.streamrecord.StreamedRecord;
import com.ericsson.component.aia.services.exteps.eh.header.AbstractHeaderHandler;
import com.ericsson.component.aia.services.exteps.eh.header.cache.CacheUtils;
import com.ericsson.component.aia.services.exteps.eh.header.cache.EventHeaderCacheHandler;
import com.ericsson.component.aia.services.exteps.io.common.statistics.EpsExtStatisticsHelper;
import com.ericsson.oss.itpf.modeling.annotation.cache.CacheMode;
import com.ericsson.oss.itpf.sdk.cache.classic.CacheProviderBean;

/**
 * Handler responsible for handling missed header Same as {@link EventHeaderCacheHandler} but the remote cache is populated by external source.
 */
public class MissedHeaderHandler extends AbstractHeaderHandler {

    private EpsExtStatisticsHelper epsExtStatisticsHelper;
    private Cache<Integer, byte[]> remoteCache;


    @Override
    protected void doInit() {
        log.info("Initializing Missed Header Handler...");
        remoteCache = getRemoteCache();
        epsExtStatisticsHelper = new EpsExtStatisticsHelper(this.getClass().getSimpleName());
        epsExtStatisticsHelper.initialiseStatistics(getEventHandlerContext());
    }

    /**
     *
     * @return - Remote cache configuration .
     */
    protected Cache<Integer, byte[]> getRemoteCache() {

        final String cacheName = getStringProperty(CACHE_NAME.getKey());
        final CacheMode cacheMode = CacheMode.valueOf(getStringProperty(CACHE_MODE.getKey()));
        final int replicas = Integer.parseInt(getStringProperty(CACHE_REPLICAS.getKey()));
        final int timeToLive = Integer.parseInt(getStringProperty(TIME_TO_LIVE.getKey()));
        final String maxEntries = getStringProperty(MAX_ENTRIES.getKey());

        return new CacheProviderBean().createOrGetCache(cacheName, CacheUtils.getCacheConfiguration(cacheMode, replicas, timeToLive, maxEntries));
    }

    @Override
    public boolean understandsURI(final String understandUri) {
        return true;
    }

    @Override
    protected void handleConnectionEvent(final StreamedRecord record) {
        //do nothing.
    }

    @Override
    protected void handleDisconnectEvent(final StreamedRecord record) {
        //do nothing.
    }

    @Override
    protected void resendHeader(final StreamedRecord record) {
        final int sourceId = record.getSourceId();
        if (remoteCache != null && remoteCache.containsKey(sourceId)) {
            final StreamedRecord connectionRecord = convertRawConnectionBytesToStreamRecord(remoteCache.get(sourceId));
            sendEvent(connectionRecord);
            log.debug("Resent header for sourceId = {}", sourceId);
            localCache.add(sourceId);
        } else {
            log.debug("Header not available in remote cache for source = {} , remoteCache = {}", sourceId, remoteCache);
        }
    }

    private StreamedRecord convertRawConnectionBytesToStreamRecord(final byte[] headerRawBytes) {

        final ByteBuf byteBuf = Unpooled.wrappedBuffer(headerRawBytes);

        final MuxConnection event = (MuxConnection) DefaultStreamOutProtocolParser.getEvent(byteBuf);
        final int sourceId = event.sourceId();
        final StreamedRecord streamedRecord = new StreamedRecord(sourceId);
        streamedRecord.setAction(StreamedRecord.Actions.CONNECT);

        final byte[] ipAddress = event.ip();
        streamedRecord.setRemoteIP(ipAddress);

        final ByteBuf buff = event.payload();
        final byte[] payload = new byte[buff.capacity()];
        buff.getBytes(0, payload);
        streamedRecord.setData(payload);

        return streamedRecord;
    }

    @Override
    public void destroyAll() {
        localCache.clear();
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
}
