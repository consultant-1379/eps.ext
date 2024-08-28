/*
 * ------------------------------------------------------------------------------
 * *****************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 * *****************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.component.aia.services.exteps.eh.header.handler;

import static com.ericsson.component.aia.services.exteps.eh.header.cache.CacheProperties.CACHE_MODE;
import static com.ericsson.component.aia.services.exteps.eh.header.cache.CacheProperties.CACHE_NAME;
import static com.ericsson.component.aia.services.exteps.eh.header.cache.CacheProperties.CACHE_REPLICAS;
import static com.ericsson.component.aia.services.exteps.eh.header.cache.CacheProperties.MAX_ENTRIES;
import static com.ericsson.component.aia.services.exteps.eh.header.cache.CacheProperties.TIME_TO_LIVE;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.cache.Cache;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;

import com.ericsson.component.aia.itpf.common.config.Configuration;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.component.aia.mediation.parsers.streamrecord.StreamedRecord;
import com.ericsson.component.aia.services.eps.coordination.EpsAdaptiveConfiguration;
import com.ericsson.component.aia.services.exteps.eh.header.cache.StubbedContext;
import com.ericsson.component.aia.services.exteps.eh.header.constants.TestHeaderConstants;

@RunWith(MockitoJUnitRunner.class)
public class MissedHeaderHandlerTest {

    private StubbedMissedHeaderHandler objectUnderTest;

    private Set<Integer> testLocalCache;

    @Mock
    private Cache<Integer, byte[]> mockedRemoteCache;

    final int sourceId = 123;

    final int mockSourceId = 720897;

    @Before
    public void setUp(){

        objectUnderTest = new StubbedMissedHeaderHandler();
        testLocalCache = new HashSet<>();

        setUpMocks();
        EventHandlerContext stubbedEventHandlerContext = new StubbedContext(getConfiguration());
        Whitebox.setInternalState(objectUnderTest,"localCache", testLocalCache);
        Whitebox.setInternalState(objectUnderTest, "eventHandlerContext", stubbedEventHandlerContext);
    }

    private Configuration getConfiguration() {
        final Map<String, Object> configMap = new HashMap<>();
        configMap.put(CACHE_NAME.getKey(), "EventHeaderCache");
        configMap.put(CACHE_MODE.getKey(), "DISTRIBUTED_ASYNC");
        configMap.put(CACHE_REPLICAS.getKey(), "2");
        configMap.put(TIME_TO_LIVE.getKey(), "-1");
        configMap.put(MAX_ENTRIES.getKey(), "100000");
        final EpsAdaptiveConfiguration epsConfig = new EpsAdaptiveConfiguration();
        epsConfig.setConfiguration(configMap);
        return epsConfig;
    }

    private void setUpMocks() {
        when(mockedRemoteCache.containsKey(mockSourceId)).thenReturn(true);
        when(mockedRemoteCache.get(mockSourceId)).thenReturn(TestHeaderConstants.NODE_CONNECTION_HEADER);
    }

    @Test
    public void testInputEvents_whenConnectionEvent_thenAddConnectionEventToLocalMapAndProcessEvent(){

        final StreamedRecord connectionEvent = TestHeaderUtils.getConnectionEvent(sourceId);
        objectUnderTest.inputEvents(connectionEvent);
        final StreamedRecord connectionOutputEvent =  (StreamedRecord)objectUnderTest.getOutputEvent().get(0);

        Assert.assertTrue(testLocalCache.contains(sourceId));
        Assert.assertEquals(StreamedRecord.Actions.CONNECT,connectionOutputEvent.getAction());
    }


    @Test
    public void testInputEvents_whenConnectionEventExistAndOnPayloadEvent_thenProcessEvent(){

        final StreamedRecord connectionEvent = TestHeaderUtils.getConnectionEvent(sourceId);
        objectUnderTest.inputEvents(connectionEvent);
        final StreamedRecord connectionOutputEvent =  (StreamedRecord)objectUnderTest.getOutputEvent().get(0);
        Assert.assertTrue(testLocalCache.contains(sourceId));
        Assert.assertEquals(StreamedRecord.Actions.CONNECT,connectionOutputEvent.getAction());

        final StreamedRecord payloadEvent = TestHeaderUtils.getPayloadEvent(sourceId);
        objectUnderTest.inputEvents(payloadEvent);
        final StreamedRecord payloadOutputEvent =  (StreamedRecord)objectUnderTest.getOutputEvent().get(1);
        Assert.assertEquals(StreamedRecord.Actions.EVENT,payloadOutputEvent.getAction());
    }


    @Test
    public void testInputEvents_whenConnectionEventDoesntExist_thenGetConnectionEventFromRemoteCache(){

        final StreamedRecord payloadEvent = TestHeaderUtils.getPayloadEvent(mockSourceId);
        objectUnderTest.doInit();
        objectUnderTest.inputEvents(payloadEvent);

        final StreamedRecord connectionOutputEvent =  (StreamedRecord)objectUnderTest.getOutputEvent().get(0);
        Assert.assertTrue(testLocalCache.contains(mockSourceId));
        Assert.assertEquals(StreamedRecord.Actions.CONNECT,connectionOutputEvent.getAction());

        final StreamedRecord payloadOutputEvent =  (StreamedRecord)objectUnderTest.getOutputEvent().get(1);
        Assert.assertEquals(StreamedRecord.Actions.EVENT,payloadOutputEvent.getAction());
    }


    class StubbedMissedHeaderHandler extends MissedHeaderHandler{

        private List<Object> outputEvents = new ArrayList<>();

        @Override
        public void sendEvent(final Object outputEvent){
            outputEvents.add(outputEvent);
        }

        public List<Object> getOutputEvent(){
            return outputEvents;
        }

        @Override
        protected Cache<Integer, byte[]> getRemoteCache() {
            return mockedRemoteCache;
        }
    }
}
