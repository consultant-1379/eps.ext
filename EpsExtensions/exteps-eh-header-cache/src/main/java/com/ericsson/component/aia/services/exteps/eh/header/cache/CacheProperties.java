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

/**
 * Defines all configurable properties of cache.
 */
public enum CacheProperties {
    CACHE_NAME("cacheName"),
    CACHE_MODE("cacheMode"),
    CACHE_REPLICAS("cacheReplicas"),
    TIME_TO_LIVE("timeToLive"),
    MAX_ENTRIES("maxEntries");

    private String key;

    /**
     * Creates an instance of cache properties for the specified key.
     *
     * @param key the property key.
     */
    CacheProperties(final String key) {
        this.key = key;
    }

    /**
     * Retrieve the property key name.
     *
     * @return The key name.
     */
    public String getKey() {
        return key;
    }
}
