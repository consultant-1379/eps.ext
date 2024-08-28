/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.component.aia.services.exteps.io.adapter.streaming.util;

import java.util.Timer;

/**
 * This class is used to monitor stream loading and to print some statistics
 *
 * @since 1.0.7
 *
 */
public class StreamLoadMonitor extends java.util.TimerTask {

    private Metrics metrics = null;

    /**
     * Constructor
     *
     * @param metrics
     */
    public StreamLoadMonitor(final Metrics metrics) {
        this(metrics, 10000);
    }

    /**
     * Overloaded Constructor
     *
     * @param metrics
     * @param period
     */
    public StreamLoadMonitor(final Metrics metrics, final long period) {
        // Save the event stream handler
        this.metrics = metrics;
        new Timer(StreamLoadMonitor.class.getSimpleName()).schedule(this, period, period);
    }

    /**
     * Output the metrics for stream loading
     */
    @Override
    public void run() {
        metrics.outputMetrics();
    }
}
