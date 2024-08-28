/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.component.aia.services.exteps.io.adapter.partition;

import com.codahale.metrics.MetricRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Gets event per node metrics
 */
public class EsiEventMetricHandlerNR {

    private static final MetricRegistry METRIC_REGISTRY = new MetricRegistry();

    private static EventStatisticsPerNode eventStatisticsPerNode;

    private static final Logger LOGGER = LoggerFactory.getLogger(EsiEventMetricHandlerNR.class);


    private EsiEventMetricHandlerNR() {}

    /**
     * Sets up the Metrics
     *
     * @param statistics
     *            the EventStatisticsPerNode
     */
    public static void setUpEventPerNodeMetrics(final EventStatisticsPerNode statistics) {
        eventStatisticsPerNode = statistics;
        createEventsPerNodeMetric();
    }

    private static void createEventsPerNodeMetric() {
        String hostname = "null";
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            LOGGER.error("Hostname could not be retrived, hostname will appear in EsiEventMetricHandlerNr as null");
        }
        METRIC_REGISTRY.register(EsiEventMetricHandlerNR.class.getName() + "&" + hostname, eventStatisticsPerNode);
    }
}
