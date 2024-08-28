/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.exteps.io.common.statistics;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.codahale.metrics.Meter;
import com.ericsson.component.aia.itpf.common.config.Configuration;
import com.ericsson.component.aia.services.eps.coordination.EpsAdaptiveConfiguration;
import com.ericsson.component.aia.services.eps.statistics.EpsStatisticsRegister;

public class EpsExtStatisticsHelperTest {

    private EpsExtStatisticsHelper objUnderTest;

    @Before
    public void setUp(){
        objUnderTest = new EpsExtStatisticsHelper(this.getClass().getSimpleName());
    }

    @Test
    public void testInitialMetric_epsStatOn_epsExtStatsOn_isStatisticOnReturnsTrue() {
        objUnderTest.initialiseStatistics(new ReturnTrueStubbedContext(new EpsAdaptiveConfiguration()));
        assertTrue(objUnderTest.isStatisticsOn());
    }

    @Test
    public void testInitialMetric_epsStatOff_epsExtStatsOn_isStatisticOnReturnsTrue() {
        objUnderTest.initialiseStatistics(new ReturnFalseStubbedContext(new EpsAdaptiveConfiguration()));
        assertTrue(objUnderTest.isStatisticsOn());
    }

    @Test
    public void testInitialMetric_epsStatOn_epsExtStatsOff_isStatisticOnReturnsTrue() {
        System.setProperty("com.ericsson.component.aia.services.epsext.statistics.off", "true");
        objUnderTest.initialiseStatistics(new ReturnTrueStubbedContext(new EpsAdaptiveConfiguration()));
        assertTrue(objUnderTest.isStatisticsOn());
    }

    @Test
    public void testInitialMetric_epsStatOff_epsExtStatsOff_isStatisticOnReturnsTrue() {
        System.setProperty("com.ericsson.component.aia.services.epsext.statistics.off", "true");
        objUnderTest.initialiseStatistics(new ReturnFalseStubbedContext(new EpsAdaptiveConfiguration()));
        assertFalse(objUnderTest.isStatisticsOn());
    }

    private class ReturnFalseStubbedContext extends StubbedContext{

        public ReturnFalseStubbedContext(Configuration configuration) {
            super(configuration);
        }

        @Override
        public Object getContextualData(final String data) {
            final EpsStatisticsRegister mockEpsStatisticsRegister = mock(EpsStatisticsRegister.class);
            doReturn(false).when(mockEpsStatisticsRegister).isStatisticsOn();
            doReturn(new Meter()).when(mockEpsStatisticsRegister).createMeter(Mockito.anyString());
            return mockEpsStatisticsRegister;
        }
    }

    private class ReturnTrueStubbedContext extends StubbedContext{
        
        public ReturnTrueStubbedContext(Configuration configuration) {
            super(configuration);
        }

        @Override
        public Object getContextualData(final String data) {
            final EpsStatisticsRegister mockEpsStatisticsRegister = mock(EpsStatisticsRegister.class);
            doReturn(true).when(mockEpsStatisticsRegister).isStatisticsOn();
            doReturn(new Meter()).when(mockEpsStatisticsRegister).createMeter(Mockito.anyString());
            return mockEpsStatisticsRegister;
        }
        
    }
}
