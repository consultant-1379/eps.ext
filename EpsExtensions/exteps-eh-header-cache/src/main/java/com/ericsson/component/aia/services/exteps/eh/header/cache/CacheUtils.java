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

import com.ericsson.oss.itpf.modeling.annotation.cache.CacheMode;
import com.ericsson.oss.itpf.modeling.annotation.cache.EvictionStrategy;
import com.ericsson.oss.itpf.sdk.cache.classic.CacheConfiguration;

/**
 * Defines utility methods for retrieving cache configuration.
 */
public class CacheUtils {

    private CacheUtils() {
    }

    /**
     * Get a new cache configuration for the specified properties.
     *
     * @param cacheMode  The name of cache mode.
     * @param replicas   The number of replicas, applied only to distributed cache.
     * @param timeToLive The time to live for every cache entry. -1 means no limit.
     * @param maxEntries The maximum number of cache entries in the cache. -1 means no limit.
     * @return The new configuration.
     */
    public static CacheConfiguration getCacheConfiguration(final CacheMode cacheMode, final int replicas,
            final int timeToLive, final String maxEntries) {

        return new CacheConfiguration.Builder()
                .timeToLive(timeToLive)
                .evictionStrategy(EvictionStrategy.LRU)
                .maxEntries(maxEntries)
                .cacheMode(cacheMode)
                .numberOfDistributedOwners(replicas).build();
    }
}
