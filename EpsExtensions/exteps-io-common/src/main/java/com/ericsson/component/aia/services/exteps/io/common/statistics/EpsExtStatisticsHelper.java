/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.exteps.io.common.statistics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Meter;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.component.aia.services.eps.EpsEngineConstants;
import com.ericsson.component.aia.services.eps.statistics.EpsStatisticsRegister;

/**
 * Helper class responsible for creating Statistical Metrics for EpsExtension components.
 * By default any component using this class will create a Meter called ClassName@eventsReceived.
 * If different Metrics are required, this class can be extended and {@link #registerStatisticsMetrics()} can be overridden to register new metics.
 */
public class EpsExtStatisticsHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(EpsExtStatisticsHelper.class);
    private static final String EPS_EXT_STATISTICS_OFF_SYS_PARAM_NAME = "com.ericsson.component.aia.services.epsext.statistics.off";

    protected EpsStatisticsRegister epsStatisticsRegister;
    protected Meter eventMeter;
    protected Boolean epsExtStatisticsOn;
    private final String clazzName;
    private String metricsSuffix = "";

    public EpsExtStatisticsHelper(final String clazzName) {
        this.clazzName = clazzName;
    }

    /**
     * Initialise statistics.
     */
    public void initialiseStatistics(EventHandlerContext eventHandlerContext) {
        if (epsExtStatisticsOn == null) {
            final String configuredStatsDisabledValue = System.getProperty(EPS_EXT_STATISTICS_OFF_SYS_PARAM_NAME, "false");
            final boolean statisticsTurnedOff = "true".equalsIgnoreCase(configuredStatsDisabledValue);
            epsExtStatisticsOn = !statisticsTurnedOff;
        }
        epsStatisticsRegister = getStatisticsRegister(eventHandlerContext);
        if (epsStatisticsRegister == null) {
            LOGGER.error("statisticsRegister should not be null");
        } else {
            if (isStatisticsOn()) {
                registerStatisticsMetrics();
            }
        }
    }

    protected EpsStatisticsRegister getStatisticsRegister(final EventHandlerContext eventHandlerContext) {
        return (EpsStatisticsRegister) eventHandlerContext.getContextualData(
                EpsEngineConstants.STATISTICS_REGISTER_CONTEXTUAL_DATA_NAME);
    }

    /**
     * Registers a Metric.
     */
    protected void registerStatisticsMetrics() {
        eventMeter = epsStatisticsRegister.createMeter(clazzName + "@eventsReceived" + metricsSuffix);
    }

    public boolean isStatisticsOn() {
        return epsExtStatisticsOn || isEpsStatisticsOn();
    }

    protected boolean isEpsStatisticsOn() {
        return (epsStatisticsRegister != null) && epsStatisticsRegister.isStatisticsOn();
    }

    /**
     * Increments eventMeter.
     */
    public void mark() {
        eventMeter.mark();
    }

    /**
     * Increments the specified meter
     *
     * @param meter
     *          Meter to increment.
     */
    public void markMeter(Meter meter) {
        meter.mark();
    }

    /**
     *
     * @param metricsSuffix
     *          Type of data.
     */
    public void setMetricsSuffix(final String metricsSuffix) {
        this.metricsSuffix = metricsSuffix;
    }
}
