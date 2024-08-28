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

import java.util.concurrent.TimeUnit;

import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;

/**
 * Gets event per node metrics
 */
public class EsiEventMetricHandler {
    private static final String METRIC_REPORTING_PERIOD_PATH = "com.ericsson.component.aia.services.exteps.io.adapter.partition."
            + "PartitionAssigner.metricReportingPeriod";

    private static final MetricRegistry METRIC_REGISTRY = new MetricRegistry();

    private static EventStatisticsPerNode eventStatisticsPerNode;

    private static Slf4jReporter slf4jReporter;

    private EsiEventMetricHandler() {}

    /**
     * Sets up the Metrics
     *
     * @param statistics
     *            the EventStatisticsPerNode
     */
    public static void setUpEventPerNodeMetrics(final EventStatisticsPerNode statistics) {
        eventStatisticsPerNode = statistics;
        createEventsPerNodeMetric();
        startLogReporter();
    }

    private static void createEventsPerNodeMetric() {
        METRIC_REGISTRY.register(EsiEventMetricHandler.class.getName() + "&" + getEpsInstance(), eventStatisticsPerNode);
    }

    private static String getEpsInstance() {
        return System.getProperty("s");
    }

    private static void startLogReporter() {
        slf4jReporter = Slf4jReporter.forRegistry(METRIC_REGISTRY).outputTo(LoggerFactory.getLogger(EsiEventMetricHandler.class))
                .convertRatesTo(TimeUnit.SECONDS).convertDurationsTo(TimeUnit.MILLISECONDS).build();
        slf4jReporter.start(
                Integer.parseInt(
                        System.getProperty(METRIC_REPORTING_PERIOD_PATH,
                                "1")),
                TimeUnit.MINUTES);
    }
}
