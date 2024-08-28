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
package com.ericsson.component.aia.services.exteps.io.adapter.streaming;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import com.codahale.metrics.Meter;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.component.aia.mediation.parsers.streamrecord.StreamedRecord;
import com.ericsson.component.aia.services.eps.EpsEngineConstants;
import com.ericsson.component.aia.services.eps.statistics.EpsStatisticsRegister;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.controller.StreamingController;

@RunWith(MockitoJUnitRunner.class)
public class StreamingInputAdapterTest {

    @Spy
    private StreamingInputAdapter spyStreamingInputAdapter;

    @Mock
    private StreamingController mockController;

    @Mock
    private StreamingConfig mockStreamingConfig;

    @Mock
    private StreamedRecord mockStreamedRecord;

    @Mock
    private EventHandlerContext mockEventHandlerContext;

    @Mock
    private EpsStatisticsRegister mockEpsStatisticsRegister;

    @Test
    public void test_onEvent_NotSupported() {
        final StreamingInputAdapter adapter = new StreamingInputAdapter(mockController);
        try {
            adapter.onEvent(null);
            fail("Should had thrown UnsupportedOperationException.");
        } catch (final UnsupportedOperationException e) {
            assertTrue(true);
        }
    }

    @Test
    public void test_understandsURI_true() {
        final StreamingInputAdapter adapter = new StreamingInputAdapter(mockController);
        assertTrue(adapter.understandsURI("streaming:/"));
    }

    @Test
    public void test_wrong_value_understandsURI_false_() {
        final StreamingInputAdapter adapter = new StreamingInputAdapter(mockController);
        assertFalse(adapter.understandsURI(""));
    }

    @Test
    public void test_null_value_understandsURI_false_() {
        final StreamingInputAdapter adapter = new StreamingInputAdapter(mockController);
        assertFalse(adapter.understandsURI(null));
    }

    @Test
    public void testDoinit() {
        mockStatisticRegister(true);
        doReturn(mockStreamingConfig).when(spyStreamingInputAdapter).getConfig();
        spyStreamingInputAdapter.doInit();
        assertNotNull(spyStreamingInputAdapter.eventMeter);
    }

    @Test
    public void testDestroy_ControllerSetToNull() {
        spyStreamingInputAdapter.controller = mockController;
        assertNotNull(spyStreamingInputAdapter.controller);
        spyStreamingInputAdapter.destroy();
        assertNull(spyStreamingInputAdapter.controller);
    }

    @Test
    public void testStreamRecieved_whenStatisticsOn() {
        mockStatisticRegister(true);
        doReturn(mockStreamingConfig).when(spyStreamingInputAdapter).getConfig();
        spyStreamingInputAdapter.doInit();
        assertEquals(0, spyStreamingInputAdapter.eventMeter.getCount());
        spyStreamingInputAdapter.streamReceived(mockStreamedRecord);
        assertEquals(1, spyStreamingInputAdapter.eventMeter.getCount());
    }

    @Test
    public void testStreamRecieved_whenStatisticsOff() {
        mockStatisticRegister(false);
        doReturn(mockStreamingConfig).when(spyStreamingInputAdapter).getConfig();
        spyStreamingInputAdapter.doInit();
        spyStreamingInputAdapter.streamReceived(mockStreamedRecord);
        assertNull(spyStreamingInputAdapter.eventMeter);
    }

    private void mockStatisticRegister(final boolean statisticsOn) {
        org.mockito.internal.util.reflection.Whitebox.setInternalState(spyStreamingInputAdapter, "eventHandlerContext", mockEventHandlerContext);
        doReturn(statisticsOn).when(mockEpsStatisticsRegister).isStatisticsOn();
        doReturn(new Meter()).when(mockEpsStatisticsRegister).createMeter(Mockito.anyString());
        doReturn(mockEpsStatisticsRegister).when(mockEventHandlerContext).getContextualData(
                EpsEngineConstants.STATISTICS_REGISTER_CONTEXTUAL_DATA_NAME);
    }

}
