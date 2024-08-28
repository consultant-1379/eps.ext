/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2013
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.component.aia.services.exteps.io.adapter.partition;

import org.json.JSONObject;

import com.codahale.metrics.Gauge;
import com.google.gson.Gson;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Track Event Statistics Per Event Node.
 */
public final class EventStatisticsPerNode implements Gauge<String> {
    private final Map<Long, Long> eventCountPerNodeMap = new ConcurrentHashMap<>();
    private final JSONObject eventCountPerNodeJsonObject = new JSONObject();

    /**
     * Get count per node as JSON String.
     *
     * @return - the eventCountPerIdMap Map
     */
    public String getEventCountPerNodeAsJsonString() {
        return new Gson().toJson(eventCountPerNodeMap);
    }

    /**
     * @param nodeBId
     *            the nodeB ID
     */
    public void setEventCountPerId(final Long nodeBId) {
        final Long oldVal = eventCountPerNodeMap.getOrDefault(nodeBId, 0L);
        eventCountPerNodeMap.put(nodeBId, oldVal + 1L);
    }

    @Override
    public String toString() {
        return eventCountPerNodeJsonObject.toString();
    }

    @Override
    public String getValue() {
        return getEventCountPerNodeAsJsonString();
    }

}
