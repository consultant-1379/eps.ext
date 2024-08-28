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

import java.text.DecimalFormat;

/**
 * This class is responsible for instrumentation.
 *
 * @since 1.0.7
 *
 **/
public class EventMetrics {

    private static EventMetrics instance;
    private transient long events = 0;
    private transient long events_per_interval = 0;
    private transient long records = 0;
    private transient long invalidRecords = 0;
    private transient long lostRecords = 0;
    private transient long connects = 0;
    private transient long disconnects = 0;
    private transient long drops = 0;
    private transient long noSrc = 0;
    private transient long startTime = 0;
    private transient long toTime = 0;
    private transient boolean monitorOn;

    public long getConnects() {
        return connects;
    }

    public void incrementConnects() {
        if (isMonitorOn()) {
            connects++;
        }
    }

    public long getDisconnects() {
        return disconnects;
    }

    public void incrementDisconnects() {
        if (isMonitorOn()) {
            disconnects++;
        }
    }

    public long getDrops() {
        return drops;
    }

    public void incrementDrops() {
        if (isMonitorOn()) {
            drops++;
        }
    }

    public long getEvents() {
        return events;
    }

    public void incrementEvents() {
        if (isMonitorOn()) {
            events++;
            events_per_interval++;
        }
    }

    public long getInvalidRecords() {
        return invalidRecords;
    }

    public void incrementInvalidRecords(final long myInvalidRecordsParam) {
        if (isMonitorOn()) {
            invalidRecords += myInvalidRecordsParam;
        }
    }

    public long getLostRecords() {
        return lostRecords;
    }

    public void incrementLostRecords(final long myLostRecordsParam) {
        if (isMonitorOn()) {
            lostRecords += myLostRecordsParam;
        }
    }

    public long getNoSrc() {
        return noSrc;
    }

    public void incrementNoSrc() {
        if (isMonitorOn()) {
            noSrc++;
        }
    }

    public long getRecords() {
        return records;
    }

    public void incrementRecords() {
        if (isMonitorOn()) {
            records++;
        }
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(final long myStartTimeParam) {
        startTime = myStartTimeParam;
    }

    public long getToTime() {
        return toTime;
    }

    public void setToTime(final long myToTimeParam) {
        toTime = myToTimeParam;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        final DecimalFormat threePlaces = new DecimalFormat("0.000");
        final double interval = (toTime - startTime) / 1000.0;
        builder.append("TotalRecords=");
        builder.append(records);
        builder.append(";TotalEvents=");
        builder.append(events);
        builder.append(";Connects=");
        builder.append(connects);
        builder.append(";Disconnects=");
        builder.append(disconnects);
        builder.append(";ActiveConnections=");
        builder.append(connects - disconnects);
        builder.append(";Drops=");
        builder.append(drops);
        builder.append(";LostEvents=");
        builder.append(lostRecords);
        builder.append(";Invalid=");
        builder.append(invalidRecords);
        builder.append(";NoSourceId=");
        builder.append(noSrc);
        builder.append(";EventsPerSec=");
        builder.append(interval > 0 ? threePlaces.format(events_per_interval / interval) : 0);
        reset();
        return builder.toString();
    }

    private void reset() {
        events_per_interval = 0;
    }

    /**
     * @return An instance of EventMetrics
     */
    public static EventMetrics getInstance() {
        if (instance == null) {
            instance = new EventMetrics();
        }
        return instance;
    }

    /**
     * @param monitorOn
     */
    public void setMonitorOn(final boolean monitorOn) {
        this.monitorOn = monitorOn;
    }

    public boolean isMonitorOn() {
        return monitorOn;
    }
}
