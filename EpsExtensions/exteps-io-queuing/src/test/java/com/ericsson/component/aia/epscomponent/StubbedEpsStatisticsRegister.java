/*
 * ------------------------------------------------------------------------------
 *  *******************************************************************************
 *  * COPYRIGHT Ericsson 2017
 *  *
 *  * The copyright to the computer program(s) herein is the property of
 *  * Ericsson Inc. The programs may be used and/or copied only with written
 *  * permission from Ericsson Inc. or in accordance with the terms and
 *  * conditions stipulated in the agreement/contract under which the
 *  * program(s) have been supplied.
 *  *******************************************************************************
 *  *----------------------------------------------------------------------------
 */
package com.ericsson.component.aia.epscomponent;

import java.util.HashMap;
import java.util.Map;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.ericsson.component.aia.itpf.common.event.handler.*;
import com.ericsson.component.aia.services.eps.statistics.*;


class StubbedEpsStatisticsRegister implements EpsStatisticsRegister {

    private final Map<String, Meter> metersMap = new HashMap<>();

    private final Map<String, Counter> countersMap = new HashMap<>();

    private final Map<String, Gauge<?>> gaugesMap = new HashMap<>();

    @Override
    public boolean isStatisticsOn() {
        return true;
    }

    @Override
    public Meter createMeter(final String meterName, final AbstractEventHandler eventHandler) {
        final Meter value = new Meter();
        metersMap.put(meterName, value);
        return value;
    }

    @Override
    public Meter createMeter(final String meterName) {
        final Meter value = new Meter();
        metersMap.put(meterName, value);
        return value;
    }

    @Override
    public Counter createCounter(final String counterName) {
        final Counter counter = new Counter();
        countersMap.put(counterName, counter);
        return counter;
    }

    @Override
    public Counter createCounter(final String counterName, final AbstractEventHandler eventHandler) {
        final Counter counter = new Counter();
        countersMap.put(counterName, counter);
        return counter;
    }

    @Override
    public void registerGuage(final String gaugeName, final Gauge<Long> gauge, final AbstractEventHandler eventHandler) {
        gaugesMap.put(gaugeName, gauge);
    }

    @Override
    public void registerGuage(final String gaugeName, final Gauge<Long> gauge) {
        gaugesMap.put(gaugeName, gauge);
    }

    @Override
    public void registerCounter(String s, Counter counter) {
        // Do nothing
    }

    @Override
    public void registerCounter(String s, Counter counter, AbstractEventHandler abstractEventHandler) {
        // Do nothing
    }

    public Meter getMeter(final String meterName) {
        return metersMap.get(meterName);
    }

    public Gauge<?> getGauge(final String gaugerName) {
        return gaugesMap.get(gaugerName);
    }


    public Counter getCounter(final String counterName){
        return countersMap.get(counterName);
    }

    public void clear(){
        metersMap.clear();
        gaugesMap.clear();
        countersMap.clear();
    }
}