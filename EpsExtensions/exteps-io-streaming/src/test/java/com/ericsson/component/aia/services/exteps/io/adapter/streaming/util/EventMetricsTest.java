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
package com.ericsson.component.aia.services.exteps.io.adapter.streaming.util;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author esarlag
 * 
 */
public class EventMetricsTest {

    static EventMetrics eventMetrics;

    @BeforeClass
    public static void setUp() {
        eventMetrics = EventMetrics.getInstance();
        eventMetrics.setMonitorOn(false);
    }

    @Test
    public void testIncrementEvents() {
        final long base = eventMetrics.getEvents();
        eventMetrics.incrementEvents();
        Assert.assertEquals("Events incremented even if monitor is off", base, eventMetrics.getEvents());
    }

    @Test
    public void testIncrementInvalidRecords() {
        final long base = eventMetrics.getInvalidRecords();
        eventMetrics.incrementInvalidRecords(1L);
        Assert.assertEquals("Invalid records incremented even if monitor is off", base, eventMetrics.getInvalidRecords());
    }

    @Test
    public void testIncrementLostRecords() {
        final long base = eventMetrics.getLostRecords();
        eventMetrics.incrementLostRecords(1L);
        Assert.assertEquals("Lost records incremented even if monitor is off", base, eventMetrics.getLostRecords());
    }

    @Test
    public void testIncrementNoSrc() {
        final long base = eventMetrics.getNoSrc();
        eventMetrics.incrementNoSrc();
        Assert.assertEquals("NoSrc incremented even if monitor is off", base, eventMetrics.getNoSrc());
    }

    @Test
    public void testIncrementRecords() {
        final long base = eventMetrics.getRecords();
        eventMetrics.incrementRecords();
        Assert.assertEquals("Records incremented even if monitor is off", base, eventMetrics.getRecords());
    }

    public void testToTime() {
        eventMetrics.setToTime(10L);
        Assert.assertEquals("To time unexpected", 10L, eventMetrics.getToTime());
    }

    @Test
    public void testToString() {
        final String string = eventMetrics.toString();
        Assert.assertNotNull(string);
        System.out.println(string);
    }

}
