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
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;

import com.codahale.metrics.Meter;
import com.ericsson.component.aia.itpf.common.event.ControlEvent;
import com.ericsson.component.aia.itpf.common.event.handler.*;
import com.ericsson.component.aia.mediation.parsers.streamrecord.StreamedRecord;
import com.ericsson.component.aia.services.eps.EpsEngineConstants;
import com.ericsson.component.aia.services.eps.statistics.EpsStatisticsRegister;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.controller.StreamingController;

/**
 * @author epiemir
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class ReactiveStreamingInputAdapterTest {

    private static final int NR_SUBSCRIBERS = 3;
    public static final String INPUT_IP = "inputIP";
    public static final String INPUT_PORT = "inputPort";
    public static final String USER_ID = "userId";
    public static final int DEFAULT_USER_ID = 1;
    public static final String FILTER_ID = "FilterId";
    public static final int DEFAULT_FILTER_ID = 0;
    public static final String GROUP_ID = "GroupId";
    public static final int DEFAULT_GROUP_ID = 2;
    public static final String STREAM_LOAD_MONITOR = "StreamLoadMonitor";
    public static final String MONITOR_PERIOD = "MonitorPeriod";

    @Mock
    private StreamingController mockStreamingController;

    private final ControlEvent controlEvent = new ControlEvent(ControlEvent.CONFIGURATION_CHANGED);
    private final Map<String, Object> data = new HashMap<>();

    @Spy
    private ReactiveStreamingInputAdapter spyReactiveStreamingAdapter;

    @Mock
    EventHandlerContext mockEventHandlerContext;

    @Mock
    private StreamingController mockController;

    @Mock
    StreamingConfig mockStreamingConfig;

    @Mock
    private StreamedRecord mockStreamedRecord;

    @Mock
    private EventInputHandler mockEventHandler;

    @Mock
    private EpsStatisticsRegister mockEpsStatisticsRegister;

    @Before
    public void setUpData() {
        MockitoAnnotations.initMocks(this);
        data.put(INPUT_IP, "1.2.3.4");
        data.put(INPUT_PORT, "12345");
        data.put(GROUP_ID, "123");
        data.put(FILTER_ID, "1");
        data.put(USER_ID, "1");
        data.put(STREAM_LOAD_MONITOR, "false");
        data.put(MONITOR_PERIOD, "10000");
        controlEvent.getData().putAll(data);

        org.mockito.internal.util.reflection.Whitebox.setInternalState(spyReactiveStreamingAdapter, "eventHandlerContext", mockEventHandlerContext);

        final Collection<EventSubscriber> subscribers = new ArrayList<EventSubscriber>();
        for (int i = 0; i < NR_SUBSCRIBERS; i++) {
            final EventSubscriber subscriber = new TestEventSubscriberImpl("id" + i, mockEventHandler);
            subscribers.add(subscriber);
        }
        doReturn(subscribers).when(mockEventHandlerContext).getEventSubscribers();

    }

    @Test
    public void shouldUnderstandCorrectURI() {
        final ReactiveStreamingInputAdapter adapter = new ReactiveStreamingInputAdapter();
        assertTrue(adapter.understandsURI("adaptive-streaming:/"));
    }

    @Test
    public void shouldNotUnderstandIncorrectURI() {
        final ReactiveStreamingInputAdapter adapter = new ReactiveStreamingInputAdapter();
        assertFalse(adapter.understandsURI(""));
        assertFalse(adapter.understandsURI("streaming:/"));
        assertFalse(adapter.understandsURI(null));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void onEvent_inputEventNull_throwsUnsupportedOperationException() {
        final ReactiveStreamingInputAdapter adapter = new ReactiveStreamingInputAdapter();
        adapter.onEvent(null);
    }

    @Test
    public void destroy_controllerIsNotNull_controllerIsSetToNull() {
        final ReactiveStreamingInputAdapter adapter = new ReactiveStreamingInputAdapter(mockController);
        assertNotNull(adapter.getStreamingController());
        adapter.destroy();
        assertNull(adapter.getStreamingController());
    }

    @Test
    public void streamReceived_statisticsEnabled_meterIncremented() {
        org.mockito.internal.util.reflection.Whitebox.setInternalState(spyReactiveStreamingAdapter, "eventHandlerContext", mockEventHandlerContext);
        doReturn(true).when(mockEpsStatisticsRegister).isStatisticsOn();
        doReturn(new Meter()).when(mockEpsStatisticsRegister).createMeter(Mockito.anyString());
        doReturn(mockEpsStatisticsRegister).when(mockEventHandlerContext).getContextualData(
                EpsEngineConstants.STATISTICS_REGISTER_CONTEXTUAL_DATA_NAME);

        doReturn(mockStreamingConfig).when(spyReactiveStreamingAdapter).getStreamingConfigFromProperties(controlEvent.getData());
        spyReactiveStreamingAdapter.doInit();

        final Meter eventMeter = Whitebox.getInternalState(spyReactiveStreamingAdapter, Meter.class);

        assertEquals(0, eventMeter.getCount());
        spyReactiveStreamingAdapter.streamReceived(mockStreamedRecord);
        Mockito.verify(mockEventHandler, Mockito.times(NR_SUBSCRIBERS)).onEvent(mockStreamedRecord);
        assertEquals(1, eventMeter.getCount());
    }

    @Test
    public void testStreamReceived_whenStatisticsOff() {
        doReturn(mockStreamingConfig).when(spyReactiveStreamingAdapter).getStreamingConfigFromProperties(controlEvent.getData());

        spyReactiveStreamingAdapter.doInit();
        spyReactiveStreamingAdapter.streamReceived(mockStreamedRecord);

        final Meter eventMeter = Whitebox.getInternalState(spyReactiveStreamingAdapter, Meter.class);
        Mockito.verify(mockEventHandler, Mockito.times(NR_SUBSCRIBERS)).onEvent(mockStreamedRecord);
        assertNull(eventMeter);
    }

    @Test
    public void shouldStartAndStopWithReconfigure() {
        doReturn(mockStreamingConfig).when(spyReactiveStreamingAdapter).getStreamingConfigFromProperties(controlEvent.getData());
        doReturn(mockStreamingController).when(spyReactiveStreamingAdapter).getNewStreamingController();
        spyReactiveStreamingAdapter.react(controlEvent);
        spyReactiveStreamingAdapter.react(controlEvent);
        verify(mockStreamingController, times(2)).start(mockStreamingConfig);
        verify(mockStreamingController).stop();
    }

    @Test
    public void shouldStopWithEmptyControlEvent() {
        doReturn(mockStreamingConfig).when(spyReactiveStreamingAdapter).getStreamingConfigFromProperties(controlEvent.getData());
        doReturn(mockStreamingController).when(spyReactiveStreamingAdapter).getNewStreamingController();
        spyReactiveStreamingAdapter.react(controlEvent);
        spyReactiveStreamingAdapter.react(new ControlEvent(ControlEvent.CONFIGURATION_CHANGED));
        verify(mockStreamingController).start(mockStreamingConfig);
        verify(mockStreamingController).stop();
    }

    @Test
    public void shouldStartStopTwiceWithReconfigureFollowedByEmptyControlEvent() {
        doReturn(mockStreamingConfig).when(spyReactiveStreamingAdapter).getStreamingConfigFromProperties(controlEvent.getData());
        doReturn(mockStreamingController).when(spyReactiveStreamingAdapter).getNewStreamingController();
        spyReactiveStreamingAdapter.react(controlEvent);
        spyReactiveStreamingAdapter.react(controlEvent);
        spyReactiveStreamingAdapter.react(new ControlEvent(ControlEvent.CONFIGURATION_CHANGED));
        verify(mockStreamingController, times(2)).start(mockStreamingConfig);
        verify(mockStreamingController, times(2)).stop();
    }

    @Test
    public void shouldNotInteractWithControllerWhenInitialControlEventHasNoData() {
        doReturn(mockStreamingConfig).when(spyReactiveStreamingAdapter).getStreamingConfigFromProperties(controlEvent.getData());
        doReturn(mockStreamingController).when(spyReactiveStreamingAdapter).getNewStreamingController();
        spyReactiveStreamingAdapter.react(new ControlEvent(ControlEvent.CONFIGURATION_CHANGED));
        verifyZeroInteractions(mockStreamingController);
    }

}
