package com.ericsson.component.aia.services.exteps.eh.parser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLStreamHandlerFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.ericsson.component.aia.itpf.common.config.Configuration;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.component.aia.itpf.common.event.handler.EventSubscriber;
import com.ericsson.component.aia.model.base.exception.ResourceNotFoundException;
import com.ericsson.component.aia.model.base.exception.SchemaException;
import com.ericsson.component.aia.model.base.meta.SchemaTypeLoader;
import com.ericsson.component.aia.model.base.util.metrics.EventMetrics;
import com.ericsson.component.aia.services.eps.EpsEngineConstants;
import com.ericsson.component.aia.services.eps.statistics.EpsStatisticsRegister;
import com.ericsson.component.aia.services.exteps.eh.parser.exception.EventFilterServiceLocatorException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JUnit test class for {@link com.ericsson.component.aia.services.exteps.eh.parser.PMFileParser}
 */
public class PMFileParserTest {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    private static final String EMPTY_VARIABLE_TYPE = "";
    private static final String NULL_VARIABLE_TYPE = null;
    private static final String UNKNOWN_SCHEMA_TYPE = "unknown";
    private static final String VALID_SCHEMA_TYPE = "CELLTRACE";
    private static final String VALID_DECODED_TYPE = "generic_record";
    private static final String VALID_EVENT_FILE_PATH = "local:/src//test//resources//eventfilter.json";
    private static final String INVALID_EVENT_PATH = "local1://ericsson/eventfilter.json";
    private static final String EVENT_FILTER_PATH = "eventFilterPath";
    private static final String INVALID_EVENT_REST_URI = "http1://eric-cm-mediator/cm/api/v1/configurations/eventlist";
    private static final String EXPECTED_URI = "http://localhost:5003/cm/api/v1/configuration/eventlist";
    private static final String FILTERLIST = "{\"baseETag\": \"1736aa534f120c565e9c9040a0c01020\", \"configETag\": \"efcd798cf93578f93aff0b5c9a6c04a2\", \"data\": {\"events\": [5153, 3108, 2244]}, \"configName\": \"eventlist\", \"event\": \"configUpdated\"}";
    private static String PATH_VALUE = "/tmp/enm/ebrs/abc-pqr.bin";

    private URL url;
    private HttpURLConnection connection;

    private static HttpUrlStreamHandler httpUrlStreamHandler;

    private final EventSubscriber mockedSubscriber = mock(EventSubscriber.class);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    final Configuration mockConfig = mock(Configuration.class);
    final EventHandlerContext mockContext = mock(EventHandlerContext.class);

    @BeforeClass
    public static void setupURLStreamHandlerFactory() {
        // Allows for mocking URL connections
        final URLStreamHandlerFactory urlStreamHandlerFactory = mock(URLStreamHandlerFactory.class);
        URL.setURLStreamHandlerFactory(urlStreamHandlerFactory);

        httpUrlStreamHandler = new HttpUrlStreamHandler();
        given(urlStreamHandlerFactory.createURLStreamHandler("http")).willReturn(httpUrlStreamHandler);
    }

    private EventHandlerContext createContext(final String schemaType, final String decodedEventType, final Boolean isStatsOn) {

        when(mockConfig.getStringProperty(PMFileParser.SCHEMA_TYPE)).thenReturn(schemaType);
        when(mockConfig.getStringProperty(PMFileParser.DECODER_TYPE)).thenReturn(decodedEventType);

        final List<EventSubscriber> subscribersList = new ArrayList<>();
        subscribersList.add(mockedSubscriber);

        when(mockContext.getEventSubscribers()).thenReturn(subscribersList);
        when(mockContext.getEventHandlerConfiguration()).thenReturn(mockConfig);

        final EpsStatisticsRegister mockEpsStatisticsRegister = mock(EpsStatisticsRegister.class);
        doReturn(isStatsOn).when(mockEpsStatisticsRegister).isStatisticsOn();
        doReturn(new Meter()).when(mockEpsStatisticsRegister).createMeter(PMFileParser.class.getSimpleName() + "@" + "records");
        doReturn(new Meter()).when(mockEpsStatisticsRegister).createMeter(PMFileParser.class.getSimpleName() + "@" + "eventsProcessed");
        doReturn(new Meter()).when(mockEpsStatisticsRegister).createMeter(PMFileParser.class.getSimpleName() + "@" + "fileprocessingtime");

        doReturn(new Counter()).when(mockEpsStatisticsRegister).createCounter(PMFileParser.class.getSimpleName() + "@" + "records");
        doReturn(new Counter()).when(mockEpsStatisticsRegister).createCounter(PMFileParser.class.getSimpleName() + "@" + "eventsProcessed");
        doReturn(new Counter()).when(mockEpsStatisticsRegister).createCounter(PMFileParser.class.getSimpleName() + "@" + "erroneousFiles");
        doReturn(new Counter()).when(mockEpsStatisticsRegister).createCounter(PMFileParser.class.getSimpleName() + "@" + "invalidEvents");
        doReturn(new Counter()).when(mockEpsStatisticsRegister).createCounter(PMFileParser.class.getSimpleName() + "@" + "ignoredEvents");
        doReturn(new Counter()).when(mockEpsStatisticsRegister).createCounter(PMFileParser.class.getSimpleName() + "@" + "filecounts");

        doReturn(mockEpsStatisticsRegister).when(mockContext).getContextualData(EpsEngineConstants.STATISTICS_REGISTER_CONTEXTUAL_DATA_NAME);

        return mockContext;
    }

    private EventHandlerContext createContext(final String eventFilterPath) {

        createContext(VALID_SCHEMA_TYPE, VALID_DECODED_TYPE, true);
        when(mockConfig.getStringProperty(EVENT_FILTER_PATH)).thenReturn(eventFilterPath);
        return mockContext;
    }

    /**
     * Test method for
     * {@link com.ericsson.component.aia.services.exteps.eh.parser.PMFileParser#init(com.ericsson.oss.itpf.common.event.handler.EventHandlerContext)}
     * .
     */
    @Test
    public void init_schemaTypeNotRecognised_IllegalStateException() {
        thrown.expect(IllegalStateException.class);
        final PMFileParser handler = new PMFileParser();
        handler.init(createContext(UNKNOWN_SCHEMA_TYPE, VALID_DECODED_TYPE, true));
    }

    /**
     * Test method for
     * {@link com.ericsson.component.aia.services.exteps.eh.parser.PMFileParser#init(com.ericsson.oss.itpf.common.event.handler.EventHandlerContext)}
     * .
     */
    @Test
    public void init_schemaTypeNotSet_IllegalStateException() {
        thrown.expect(IllegalStateException.class);
        final PMFileParser handler = new PMFileParser();
        handler.init(createContext(EMPTY_VARIABLE_TYPE, VALID_DECODED_TYPE, true));
    }

    /**
     * Test method for
     * {@link com.ericsson.component.aia.services.exteps.eh.parser.PMFileParser#init(com.ericsson.oss.itpf.common.event.handler.EventHandlerContext)}
     * .
     */
    @Test
    public void init_schemaTypeNull_IllegalStateException() {
        thrown.expect(IllegalStateException.class);
        final PMFileParser handler = new PMFileParser();
        handler.init(createContext(NULL_VARIABLE_TYPE, VALID_DECODED_TYPE, true));
    }

    @Test
    public void testItLoadSchemaDefinitionOnStartUp() {
        final PMFileParser handler = new PMFileParser();
        handler.init(createContext(VALID_SCHEMA_TYPE, VALID_DECODED_TYPE, true));
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

    @Test
    public void processInputFiles_validJsonString_invalidFilePath() {
        final PMFileParser handler = new PMFileParser();
        handler.init(createContext(VALID_SCHEMA_TYPE, VALID_DECODED_TYPE, true));
        final String path = "{\"path\" : \"/tmp/enm/ebrs/abc-pqr.bin\"}";
        final EventMetrics metrics = handler.processInputFiles(FileInformationRetrieverUtil.getListOfFiles(path));
        assert (metrics.getErroneousFiles() == 1);
    }

    @Test
    public void processInputFiles_invalidJsonString_noProcessing() {
        final PMFileParser handler = new PMFileParser();
        handler.init(createContext(VALID_SCHEMA_TYPE, VALID_DECODED_TYPE, true));
        final String path = "{}";
        final EventMetrics metrics = handler.processInputFiles(FileInformationRetrieverUtil.getListOfFiles(path));
        assert (metrics == null);
    }

    @Test
    public void onEvent_InvalidFilePath_StatisticsOnCountErroneousFileCount() {
        final PMFileParser handler = new PMFileParser();
        handler.init(createContext(VALID_SCHEMA_TYPE, VALID_DECODED_TYPE, true));
        final String path = "{\"path\" : \"/tmp/enm/ebrs/abc-pqr.bin\"}";
        handler.onEvent(path);
        assert (handler.erroneousFilesCounter.getCount() == 1);
        assert (handler.invalidEventsCounter.getCount() == 0);
        assert (handler.eventsProcessedMeter.getCount() == 0);
        assert (handler.recordsMeter.getCount() == 0);
        assert (handler.ignoredEventsCounter.getCount() == 0);
        assert (handler.filesCountCounter.getCount() == 1);
    }

    @Test
    public void onEvent_InvalidFilePath_StatisticsOffCountErroneousFileCount() {

        final PMFileParser handler = new PMFileParser();
        handler.init(createContext(VALID_SCHEMA_TYPE, VALID_DECODED_TYPE, false));
        final String path = "{\"path\" : \"/tmp/enm/ebrs/abc-pqr.bin\"}";
        handler.onEvent(path);
        assert (handler.erroneousFilesCounter == null);
        assert (handler.invalidEventsCounter == null);
        assert (handler.eventsProcessedMeter == null);
        assert (handler.recordsMeter == null);
        assert (handler.ignoredEventsCounter == null);
        assert (handler.filesCountCounter == null);

    }

    @Test
    public void retrieveSupportedEvents_validEventFile_events() throws EventFilterServiceLocatorException {
        final PMFileParser handler = new PMFileParser();
        handler.init(createContext(VALID_EVENT_FILE_PATH));
        final Set<Integer> events = handler.retrieveSupportedEvents();
        assert (events.size() == 2);
    }

    @Test
    public void retrieveSupportedEvents_nullFilePath_emptySet() throws EventFilterServiceLocatorException {

        final PMFileParser handler = new PMFileParser();
        handler.init(createContext(NULL_VARIABLE_TYPE));
        final Set<Integer> events = handler.retrieveSupportedEvents();
        assert (events.size() == 0);
    }

    @Test
    public void retrieveSupportedEvents_emptyFilePath_emptySet() {

        final PMFileParser handler = new PMFileParser();
        handler.init(createContext(EMPTY_VARIABLE_TYPE));
        final Set<Integer> events = handler.retrieveSupportedEvents();
        assert (events.size() == 0);
    }

    @Test
    public void retrieveSupportedEvents_inValidEventFilterPath_throwException() throws EventFilterServiceLocatorException {
        thrown.expect(RuntimeException.class);
        final PMFileParser handler = new PMFileParser();
        handler.init(createContext(INVALID_EVENT_PATH));
        handler.retrieveSupportedEvents();
    }

    @Test
    public void retrieveSupportedEvents_invalidEventFilterRestURI_throwException() {
        thrown.expect(EventFilterServiceLocatorException.class);
        final PMFileParser handler = new PMFileParser();
        handler.init(createContext(INVALID_EVENT_REST_URI));
        final Set<Integer> events = handler.retrieveSupportedEvents();
    }

    @Test
    public void retrieveSupportedEvents_validEventFilterRestURL_eventTypeSet() throws Exception {

        mockRestService(EXPECTED_URI);

        final PMFileParser handler = new PMFileParser();
        handler.init(createContext(EXPECTED_URI));

        mockRestService(EXPECTED_URI);
        final Set<Integer> events = handler.retrieveSupportedEvents();
        assert (events.size() == 3);
    }

    private void mockRestService(final String EXPECTED_URI) throws Exception {
        connection = mock(HttpURLConnection.class);
        httpUrlStreamHandler.addConnection(new URL(EXPECTED_URI), connection);
        when(connection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK).thenReturn(HttpURLConnection.HTTP_OK);
        final InputStream inputStream = new ByteArrayInputStream(FILTERLIST.getBytes());
        when(connection.getInputStream()).thenReturn(inputStream).thenReturn(inputStream);
    }

    @Test
    public void isFilePathEvent_validCollectionOfFiles_true() {
        final Collection<File> collectionInput = new ArrayList<>();
        collectionInput.add(new File(PATH_VALUE));
        collectionInput.add(new File(PATH_VALUE));
        final Object input = collectionInput;
        final PMFileParser handler = new PMFileParser();
        final boolean result = handler.isFilePathEvent(input);
        assertTrue(result);
    }

    @Test
    public void isFilePathEvent_validFile_true() {
        final File file = new File(PATH_VALUE);
        final Object input = file;
        final PMFileParser handler = new PMFileParser();
        final boolean result = handler.isFilePathEvent(input);
        assertTrue(result);
    }

    @Test
    public void isFilePathEvent_validJsonString_true() {
        final String validJsonString = "{\"path\" : \"/tmp/enm/ebrs/abc-pqr.bin\"}";
        final PMFileParser handler = new PMFileParser();
        final boolean result = handler.isFilePathEvent(validJsonString);
        assertTrue(result);
    }

    @Test
    public void isFilePathEvent_emptyJsonString_false() {
        final String emptyJsonString = "{}";
        final PMFileParser handler = new PMFileParser();
        final boolean result = handler.isFilePathEvent(emptyJsonString);
        assertFalse(result);
    }

    @Test
    public void isFilePathEvent_invalidJsonString_false() {
        final String invalidJsonString = "}";
        final PMFileParser handler = new PMFileParser();
        final boolean result = handler.isFilePathEvent(invalidJsonString);
        assertFalse(result);
    }

    @Test
    public void isFilePathEvent_validJsonNode_true() {
        final String path = "{\"path\" : \"/tmp/enm/ebrs/abc-pqr.bin\"}";
        final ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = mapper.readTree(path);
        } catch (final IOException e) {
            Assert.fail("Error while parsing Json string for JsonNode");
        }
        final PMFileParser handler = new PMFileParser();
        final boolean result = handler.isFilePathEvent(jsonNode);
        assertTrue(result);
    }

    @Test
    public void isFilePathEvent_invalidJsonNode_false() {
        final String invalidJsonNode = "{\"paths\" : \"/tmp/enm/ebrs/abc-pqr.bin\"}";
        final ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = mapper.readTree(invalidJsonNode);
        } catch (final IOException e) {
            Assert.fail("Error while parsing Json string for JsonNode");
        }
        final PMFileParser handler = new PMFileParser();
        final boolean result = handler.isFilePathEvent(jsonNode);
        assertFalse(result);
    }

}