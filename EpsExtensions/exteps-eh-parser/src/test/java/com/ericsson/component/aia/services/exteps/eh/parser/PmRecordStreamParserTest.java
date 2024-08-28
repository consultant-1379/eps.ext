package com.ericsson.component.aia.services.exteps.eh.parser;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Meter;
import com.ericsson.component.aia.services.exteps.eh.parser.PmRecordStreamParser;
import com.ericsson.component.aia.itpf.common.config.Configuration;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.component.aia.itpf.common.event.handler.EventSubscriber;
import com.ericsson.component.aia.mediation.parsers.exception.LoadingFailedException;
import com.ericsson.component.aia.mediation.parsers.exception.ParsingFailedException;
import com.ericsson.component.aia.mediation.parsers.parser.Parser;
import com.ericsson.component.aia.model.base.config.bean.SchemaProviderType;
import com.ericsson.component.aia.mediation.parsers.parser.StreamParser;
import com.ericsson.component.aia.mediation.parsers.receiver.DecodedEventReceiver;
import com.ericsson.component.aia.mediation.parsers.streamrecord.StreamedRecord;
import com.ericsson.component.aia.model.base.config.bean.DecodedEventType;
import com.ericsson.component.aia.model.base.config.bean.SchemaEnum;
import com.ericsson.component.aia.model.base.exception.ResourceNotFoundException;
import com.ericsson.component.aia.model.base.exception.SchemaException;
import com.ericsson.component.aia.model.base.meta.SchemaTypeLoader;
import com.ericsson.component.aia.model.eventbean.EventBean;
import com.ericsson.component.aia.services.eps.EpsEngineConstants;
import com.ericsson.component.aia.services.eps.statistics.EpsStatisticsRegister;

public class PmRecordStreamParserTest {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String EMPTY_VARIABLE_TYPE = "";
    private static final String NULL_VARIABLE_TYPE = null;
    private static final String UNKNOWN_SCHEMA_TYPE = "unknown";
    private static final String VALID_SCHEMA_TYPE = "CELLTRACE";
    private static final String VALID_SUBNETWORK = "Dublin";
    private static final String VALID_UTCOFFSET = "+00.00";
    private static final String VALID_DECODED_TYPE = "pojo";
    private static final int SOURCEID = 123456;

    private static final byte[] ipAddress = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0A, 0x2D, 0x5A, 0x01 };

    private final EventSubscriber mockedSubscriber = mock(EventSubscriber.class);
    final EventBean mockEventBean = mock(EventBean.class);
    final Map<Integer, Parser> mockParserMap = mock(Map.class);
    final Set<Integer> mockInvalidSrcSet = mock(Set.class);
    final PmRecordStreamParser handler = spy(new PmRecordStreamParser(mockParserMap, mockInvalidSrcSet));
    MyParser stubParser = null;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private EventHandlerContext createContext(final String schemaType, final String subnetwork, final String utcOffset, final String decodedEventType) {

        final Configuration mockConfig = mock(Configuration.class);
        when(mockConfig.getStringProperty(PmRecordStreamParser.SCHEMA_TYPE)).thenReturn(schemaType);
        when(mockConfig.getStringProperty(PmRecordStreamParser.SUBNETWORK)).thenReturn(subnetwork);
        when(mockConfig.getStringProperty(PmRecordStreamParser.UTC_OFFSET)).thenReturn(utcOffset);
        when(mockConfig.getStringProperty(PmRecordStreamParser.DECODER_TYPE)).thenReturn(decodedEventType);

        final List<EventSubscriber> subscribersList = new ArrayList<>();
        subscribersList.add(mockedSubscriber);

        final EventHandlerContext mockContext = mock(EventHandlerContext.class);
        when(mockContext.getEventSubscribers()).thenReturn(subscribersList);
        when(mockContext.getEventHandlerConfiguration()).thenReturn(mockConfig);

        final EpsStatisticsRegister mockEpsStatisticsRegister = mock(EpsStatisticsRegister.class);
        doReturn(true).when(mockEpsStatisticsRegister).isStatisticsOn();
        doReturn(new Meter()).when(mockEpsStatisticsRegister).createMeter(Mockito.anyString());
        doReturn(mockEpsStatisticsRegister).when(mockContext).getContextualData(EpsEngineConstants.STATISTICS_REGISTER_CONTEXTUAL_DATA_NAME);

        return mockContext;
    }

    /**
     * Test method for
     * {@link com.ericsson.component.aia.services.exteps.eh.parser.PmRecordStreamParser#init(com.ericsson.oss.itpf.common.event.handler.EventHandlerContext)} .
     */
    @Test
    public void init_schemaTypeNotRecognised_IllegalStateException() {
        thrown.expect(IllegalStateException.class);
        final PmRecordStreamParser handler = new PmRecordStreamParser();
        handler.init(createContext(UNKNOWN_SCHEMA_TYPE, VALID_SUBNETWORK, VALID_UTCOFFSET, VALID_DECODED_TYPE));
    }

    /**
     * Test method for
     * {@link com.ericsson.component.aia.services.exteps.eh.parser.PmRecordStreamParser#init(com.ericsson.oss.itpf.common.event.handler.EventHandlerContext)} .
     */
    @Test
    public void init_schemaTypeNotSet_IllegalStateException() {
        thrown.expect(IllegalStateException.class);
        final PmRecordStreamParser handler = new PmRecordStreamParser();
        handler.init(createContext(EMPTY_VARIABLE_TYPE, VALID_SUBNETWORK, VALID_UTCOFFSET, VALID_DECODED_TYPE));
    }

    /**
     * Test method for
     * {@link com.ericsson.component.aia.services.exteps.eh.parser.PmRecordStreamParser#init(com.ericsson.oss.itpf.common.event.handler.EventHandlerContext)} .
     */
    @Test
    public void init_schemaTypeNull_IllegalStateException() {
        thrown.expect(IllegalStateException.class);
        final PmRecordStreamParser handler = new PmRecordStreamParser();
        handler.init(createContext(NULL_VARIABLE_TYPE, VALID_SUBNETWORK, VALID_UTCOFFSET, VALID_DECODED_TYPE));
    }

    /**
     * Test method for
     * {@link com.ericsson.component.aia.services.exteps.eh.parser.PmRecordStreamParser#init(com.ericsson.oss.itpf.common.event.handler.EventHandlerContext)} .
     */
    @Test
    public void init_subnetworkNotSet_IllegalStateException() {
        thrown.expect(IllegalStateException.class);
        final PmRecordStreamParser handler = new PmRecordStreamParser();
        handler.init(createContext(VALID_SCHEMA_TYPE, EMPTY_VARIABLE_TYPE, VALID_UTCOFFSET, VALID_DECODED_TYPE));
    }

    /**
     * Test method for
     * {@link com.ericsson.component.aia.services.exteps.eh.parser.PmRecordStreamParser#init(com.ericsson.oss.itpf.common.event.handler.EventHandlerContext)} .
     */
    @Test
    public void init_subnetworkNull_IllegalStateException() {
        thrown.expect(IllegalStateException.class);
        final PmRecordStreamParser handler = new PmRecordStreamParser();
        handler.init(createContext(VALID_SCHEMA_TYPE, NULL_VARIABLE_TYPE, VALID_UTCOFFSET, VALID_DECODED_TYPE));
    }

    /**
     * Test method for
     * {@link com.ericsson.component.aia.services.exteps.eh.parser.PmRecordStreamParser#init(com.ericsson.oss.itpf.common.event.handler.EventHandlerContext)} .
     */
    @Test
    public void init_utcOffsetNotSet_IllegalStateException() {
        thrown.expect(IllegalStateException.class);
        final PmRecordStreamParser handler = new PmRecordStreamParser();
        handler.init(createContext(VALID_SCHEMA_TYPE, VALID_SUBNETWORK, EMPTY_VARIABLE_TYPE, VALID_DECODED_TYPE));
    }

    /**
     * Test method for
     * {@link com.ericsson.component.aia.services.exteps.eh.parser.PmRecordStreamParser#init(com.ericsson.oss.itpf.common.event.handler.EventHandlerContext)} .
     */
    @Test
    public void init_utcOffsetNull_IllegalStateException() {
        thrown.expect(IllegalStateException.class);
        final PmRecordStreamParser handler = new PmRecordStreamParser();
        handler.init(createContext(VALID_SCHEMA_TYPE, VALID_SUBNETWORK, NULL_VARIABLE_TYPE, VALID_DECODED_TYPE));
    }

    /**
     * Test method for
     * {@link com.ericsson.component.aia.services.exteps.eh.parser.PmRecordStreamParser#onEvent(com.ericsson.oss.mediation.parsers.steamrecord.StreamedRecord)}
     */
    @Test
    public void onEvent_ConnectEvent_ParserAddedtoHashMap() {
        try {
            stubParser = new MyParser(mockEventBean, handler);
        } catch (final ParsingFailedException e) {
            log.error("ParsingFailedException thrown while creating MyParser: " + e);
        }
        when(mockParserMap.get(SOURCEID)).thenReturn(stubParser);
        handler.init(createContext(VALID_SCHEMA_TYPE, VALID_SUBNETWORK, VALID_UTCOFFSET, VALID_DECODED_TYPE));
        doReturn(stubParser).when(handler).getParser();

        final StreamedRecord mockConnectRecord = mock(StreamedRecord.class);
        when(mockConnectRecord.getSourceId()).thenReturn(SOURCEID);
        when(mockConnectRecord.getAction()).thenReturn(StreamedRecord.Actions.CONNECT);
        when(mockConnectRecord.getRemoteIP()).thenReturn(ipAddress);
        handler.onEvent(mockConnectRecord);

        verify(mockParserMap).put(SOURCEID, stubParser);
        verify(mockedSubscriber).sendEvent(mockEventBean);
        verify(mockInvalidSrcSet, never()).add(SOURCEID);
    }

    /**
     * Test method for
     * {@link com.ericsson.component.aia.services.exteps.eh.parser.PmRecordStreamParser#onEvent(com.ericsson.oss.mediation.parsers.steamrecord.StreamedRecord)}
     */
    @Test
    public void onEvent_ConnectEvent_StatisticsOn() {
        try {
            stubParser = new MyParser(mockEventBean, handler);
        } catch (final ParsingFailedException e) {
            log.error("ParsingFailedException thrown while creating MyParser: " + e);
            e.printStackTrace();
        }
        when(mockParserMap.get(SOURCEID)).thenReturn(stubParser);
        handler.init(createContext(VALID_SCHEMA_TYPE, VALID_SUBNETWORK, VALID_UTCOFFSET, VALID_DECODED_TYPE));
        doReturn(stubParser).when(handler).getParser();

        final StreamedRecord mockConnectRecord = mock(StreamedRecord.class);
        when(mockConnectRecord.getSourceId()).thenReturn(SOURCEID);
        when(mockConnectRecord.getAction()).thenReturn(StreamedRecord.Actions.CONNECT);
        when(mockConnectRecord.getRemoteIP()).thenReturn(ipAddress);
        handler.onEvent(mockConnectRecord);

        verify(mockParserMap).put(SOURCEID, stubParser);
        verify(mockedSubscriber).sendEvent(mockEventBean);
        verify(mockInvalidSrcSet, never()).add(SOURCEID);
    }

    /**
     * Test method for
     * {@link com.ericsson.component.aia.services.exteps.eh.parser.PmRecordStreamParser#onEvent(com.ericsson.oss.mediation.parsers.steamrecord.StreamedRecord)}
     * .
     */
    @Test
    public void onEvent_DisconnectEvent_ParserRemovedfromHashMap() {
        try {
            stubParser = new MyParser(mockEventBean, handler);
        } catch (final ParsingFailedException e) {
            log.error("ParsingFailedException thrown while creating MyParser: " + e);
            e.printStackTrace();
        }
        when(mockParserMap.get(SOURCEID)).thenReturn(stubParser);
        handler.init(createContext(VALID_SCHEMA_TYPE, VALID_SUBNETWORK, VALID_UTCOFFSET, VALID_DECODED_TYPE));
        doReturn(stubParser).when(handler).getParser();
        final StreamedRecord mockConnectRecord = mock(StreamedRecord.class);
        when(mockConnectRecord.getSourceId()).thenReturn(SOURCEID);
        when(mockConnectRecord.getAction()).thenReturn(StreamedRecord.Actions.CONNECT);
        when(mockConnectRecord.getRemoteIP()).thenReturn(ipAddress);
        handler.onEvent(mockConnectRecord);

        final StreamedRecord mockDisconnectRecord = mock(StreamedRecord.class);
        when(mockDisconnectRecord.getSourceId()).thenReturn(SOURCEID);
        when(mockDisconnectRecord.getAction()).thenReturn(StreamedRecord.Actions.DISCONNECT);
        handler.onEvent(mockDisconnectRecord);

        verify(mockParserMap).put(SOURCEID, stubParser);
        verify(mockParserMap).remove(SOURCEID);
        verify(mockInvalidSrcSet, never()).add(SOURCEID);
    }

    /**
     * Test method for
     * {@link com.ericsson.component.aia.services.exteps.eh.parser.PmRecordStreamParser#onEvent(com.ericsson.oss.mediation.parsers.steamrecord.StreamedRecord)}
     * .
     */
    @Test
    public void onEvent_RegularEvent_ParserRetrievedfromHashMapandExecuted() {
        try {
            stubParser = new MyParser(mockEventBean, handler);
        } catch (final ParsingFailedException e) {
            log.error("ParsingFailedException thrown while creating MyParser: " + e);
            e.printStackTrace();
        }
        when(mockParserMap.get(SOURCEID)).thenReturn(stubParser);
        handler.init(createContext(VALID_SCHEMA_TYPE, VALID_SUBNETWORK, VALID_UTCOFFSET, VALID_DECODED_TYPE));
        doReturn(stubParser).when(handler).getParser();
        final StreamedRecord mockConnectRecord = mock(StreamedRecord.class);
        when(mockConnectRecord.getSourceId()).thenReturn(SOURCEID);
        when(mockConnectRecord.getAction()).thenReturn(StreamedRecord.Actions.CONNECT);
        handler.onEvent(mockConnectRecord);

        final StreamedRecord mockEventrecord = mock(StreamedRecord.class);
        when(mockEventrecord.getSourceId()).thenReturn(SOURCEID);
        when(mockEventrecord.getAction()).thenReturn(StreamedRecord.Actions.EVENT);
        handler.onEvent(mockEventrecord);

        verify(mockParserMap).put(SOURCEID, stubParser);
        verify(mockParserMap, times(1)).get(SOURCEID);
        verify(mockedSubscriber, times(2)).sendEvent(mockEventBean);
        verify(mockInvalidSrcSet, never()).add(SOURCEID);
    }

    /**
     * Test method for
     * {@link com.ericsson.component.aia.services.exteps.eh.parser.PmRecordStreamParser#onEvent(com.ericsson.oss.mediation.parsers.steamrecord.StreamedRecord)}
     * .
     */
    @Test
    public void onEvent_ActionUnSetEvent_NoAction() {
        try {
            stubParser = new MyParser(mockEventBean, handler);
        } catch (final ParsingFailedException e) {
            log.error("ParsingFailedException thrown while creating MyParser: " + e);
            e.printStackTrace();
        }
        when(mockParserMap.get(SOURCEID)).thenReturn(stubParser);
        handler.init(createContext(VALID_SCHEMA_TYPE, VALID_SUBNETWORK, VALID_UTCOFFSET, VALID_DECODED_TYPE));
        doReturn(stubParser).when(handler).getParser();

        final StreamedRecord mockUnsetActionRecord = mock(StreamedRecord.class);
        when(mockUnsetActionRecord.getSourceId()).thenReturn(SOURCEID);
        when(mockUnsetActionRecord.getAction()).thenReturn(StreamedRecord.Actions.UNSET);
        handler.onEvent(mockUnsetActionRecord);

        verify(mockParserMap, never()).put(SOURCEID, stubParser);
        verify(mockParserMap, never()).get(SOURCEID);
        verify(mockedSubscriber, never()).sendEvent(mockEventBean);
        verify(mockInvalidSrcSet, never()).add(SOURCEID);
    }

    /**
     * Test method for
     * {@link com.ericsson.component.aia.services.exteps.eh.parser.PmRecordStreamParser#onEvent(com.ericsson.oss.mediation.parsers.steamrecord.StreamedRecord)}
     * .
     */
    @Test
    public void onEvent_InValidEventParserIOException_NoAction() {
        try {
            stubParser = spy(new MyParser(mockEventBean, handler));
        } catch (final ParsingFailedException e1) {
            log.error("ParsingFailedException thrown while creating MyParser: " + e1);
            e1.printStackTrace();
        }
        when(mockParserMap.get(SOURCEID)).thenReturn(stubParser);
        handler.init(createContext(VALID_SCHEMA_TYPE, VALID_SUBNETWORK, VALID_UTCOFFSET, VALID_DECODED_TYPE));
        doReturn(stubParser).when(handler).getParser();

        final StreamedRecord mockConnectRecord = mock(StreamedRecord.class);
        when(mockConnectRecord.getSourceId()).thenReturn(SOURCEID);
        when(mockConnectRecord.getAction()).thenReturn(StreamedRecord.Actions.CONNECT);
        try {
            doThrow(new IOException()).when(stubParser).execute(mockConnectRecord);
        } catch (final IOException e) {
            fail("unexpected IOException " + e);
        } catch (final LoadingFailedException e) {
            fail("unexpected LoadingFailedException " + e);
        }
        handler.onEvent(mockConnectRecord);

        verify(mockInvalidSrcSet).add(SOURCEID);
        verify(mockParserMap, never()).put(SOURCEID, stubParser);
        verify(mockedSubscriber, never()).sendEvent(mockEventBean);
    }

    /**
     * Test method for
     * {@link com.ericsson.component.aia.services.exteps.eh.parser.PmRecordStreamParser#onEvent(com.ericsson.oss.mediation.parsers.steamrecord.StreamedRecord)}
     * .
     */
    @Test
    public void onEvent_InValidEventParserLoadingFailedException_NoAction() {
        try {
            stubParser = spy(new MyParser(mockEventBean, handler));
        } catch (final ParsingFailedException e1) {
            log.error("ParsingFailedException thrown while creating MyParser: " + e1);
        }
        when(mockParserMap.get(SOURCEID)).thenReturn(stubParser);
        handler.init(createContext(VALID_SCHEMA_TYPE, VALID_SUBNETWORK, VALID_UTCOFFSET, VALID_DECODED_TYPE));
        doReturn(stubParser).when(handler).getParser();

        final StreamedRecord mockConnectRecord = mock(StreamedRecord.class);
        when(mockConnectRecord.getSourceId()).thenReturn(SOURCEID);
        when(mockConnectRecord.getAction()).thenReturn(StreamedRecord.Actions.CONNECT);
        try {
            doThrow(new LoadingFailedException("")).when(stubParser).execute(mockConnectRecord);
        } catch (final IOException e) {
            fail("unexpected IOException " + e);
        } catch (final LoadingFailedException e) {
            fail("unexpected LoadingFailedException " + e);
        }
        handler.onEvent(mockConnectRecord);

        verify(mockInvalidSrcSet).add(SOURCEID);
        verify(mockParserMap, never()).put(SOURCEID, stubParser);
        verify(mockedSubscriber, never()).sendEvent(mockEventBean);
    }

    /**
     * Test method for
     * {@link com.ericsson.component.aia.services.exteps.eh.parser.PmRecordStreamParser#onEvent(com.ericsson.oss.mediation.parsers.steamrecord.StreamedRecord)}
     * .
     */
    @Test
    public void onEvent_InValidEventFollowedbyValidEvent_ParserExecuted() {
        try {
            stubParser = spy(new MyParser(mockEventBean, handler));
        } catch (final ParsingFailedException e1) {
            log.error("ParsingFailedException thrown while creating MyParser: " + e1);
        }
        when(mockParserMap.get(SOURCEID)).thenReturn(stubParser);
        handler.init(createContext(VALID_SCHEMA_TYPE, VALID_SUBNETWORK, VALID_UTCOFFSET, VALID_DECODED_TYPE));
        doReturn(stubParser).when(handler).getParser();

        final StreamedRecord mockConnectRecord = mock(StreamedRecord.class);
        when(mockConnectRecord.getSourceId()).thenReturn(SOURCEID);
        when(mockConnectRecord.getAction()).thenReturn(StreamedRecord.Actions.CONNECT);
        when(mockInvalidSrcSet.contains(SOURCEID)).thenReturn(Boolean.TRUE);
        try {
            doThrow(new IOException()).when(stubParser).execute(mockConnectRecord);
        } catch (final IOException e) {
            fail("unexpected IOException " + e);
        } catch (final LoadingFailedException e) {
            fail("unexpected LoadingFailedException " + e);
        }
        handler.onEvent(mockConnectRecord);

        verify(mockInvalidSrcSet).add(SOURCEID);
        verify(mockParserMap, never()).put(SOURCEID, stubParser);
        verify(mockParserMap, never()).get(SOURCEID);
        verify(mockedSubscriber, never()).sendEvent(mockEventBean);
        try {
            doCallRealMethod().when(stubParser).execute(mockConnectRecord);
        } catch (final IOException e) {
            fail("unexpected IOException " + e);
        } catch (final LoadingFailedException e) {
            fail("unexpected LoadingFailedException " + e);
        }
        handler.onEvent(mockConnectRecord);
        verify(mockInvalidSrcSet).remove(SOURCEID);
        verify(mockParserMap).put(SOURCEID, stubParser);
    }

    /**
     * Test method for
     * {@link com.ericsson.component.aia.services.exteps.eh.parser.PmRecordStreamParser#onEvent(com.ericsson.oss.mediation.parsers.steamrecord.StreamedRecord)}
     * .
     */
    @Test
    public void onEvent_InValidEventFollowedbyRegularEvent_NoAction() {
        try {
            stubParser = spy(new MyParser(mockEventBean, handler));
        } catch (final ParsingFailedException e1) {
            log.error("ParsingFailedException thrown while creating MyParser: " + e1);
        }
        handler.init(createContext(VALID_SCHEMA_TYPE, VALID_SUBNETWORK, VALID_UTCOFFSET, VALID_DECODED_TYPE));
        doReturn(stubParser).when(handler).getParser();

        final StreamedRecord mockConnectRecord = mock(StreamedRecord.class);
        when(mockConnectRecord.getSourceId()).thenReturn(SOURCEID);
        when(mockConnectRecord.getAction()).thenReturn(StreamedRecord.Actions.CONNECT);
        when(mockInvalidSrcSet.contains(SOURCEID)).thenReturn(Boolean.TRUE);
        when(mockParserMap.get(SOURCEID)).thenReturn(null);
        try {
            doThrow(new IOException()).when(stubParser).execute(mockConnectRecord);
        } catch (final IOException e) {
            fail("unexpected IOException " + e);
        } catch (final LoadingFailedException e) {
            fail("unexpected LoadingFailedException " + e);
        }
        handler.onEvent(mockConnectRecord);

        verify(mockInvalidSrcSet).add(SOURCEID);
        verify(mockParserMap, never()).put(SOURCEID, stubParser);
        verify(mockParserMap, never()).get(SOURCEID);
        verify(mockedSubscriber, never()).sendEvent(mockEventBean);

        final StreamedRecord mockEventrecord = mock(StreamedRecord.class);
        when(mockEventrecord.getSourceId()).thenReturn(SOURCEID);
        when(mockEventrecord.getAction()).thenReturn(StreamedRecord.Actions.EVENT);
        handler.onEvent(mockEventrecord);
        verify(mockedSubscriber, never()).sendEvent(mockEventBean);

    }

    @Test
    public void testItLoadSchemaDefinitionOnStartUp() {
        final PmRecordStreamParser handler = new PmRecordStreamParser();
        handler.init(createContext(VALID_SCHEMA_TYPE, VALID_SUBNETWORK, VALID_UTCOFFSET, VALID_DECODED_TYPE));
        SchemaTypeLoader schemaTypeLoader = null;
        try {
            schemaTypeLoader = new SchemaTypeLoader();
        } catch (final ResourceNotFoundException e) {
            log.error("ResourceNotFoundException thrown while creating SchemaTypeLoader: " + e);
        } catch (final SchemaException e) {
            log.error("SchemaException thrown while creating SchemaTypeLoader: " + e);
        }
        assertTrue(schemaTypeLoader.getSchemaTypeMap().size() > 0);
    }

    class MyParser extends StreamParser {

        private final EventBean eventBean;
        private final DecodedEventReceiver decodedEventReceiver;

        MyParser(final EventBean eventBean, final PmRecordStreamParser pmRecordStreamParser) throws ParsingFailedException {
            super(SchemaEnum.CELLTRACE, SchemaProviderType.FILE_BASED, pmRecordStreamParser.getEventBeanListener(), "", "", DecodedEventType.POJO);
            this.eventBean = eventBean;
            this.decodedEventReceiver = pmRecordStreamParser.getEventBeanListener();
        }



        @Override
        public void execute(final StreamedRecord streamedRecord) throws IOException, LoadingFailedException {
            decodedEventReceiver.decodedEventPublisher(eventBean);
        }
    }
}
