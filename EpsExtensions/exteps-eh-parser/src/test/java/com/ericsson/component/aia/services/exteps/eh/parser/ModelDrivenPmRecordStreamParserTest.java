package com.ericsson.component.aia.services.exteps.eh.parser;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Meter;
import com.ericsson.component.aia.itpf.common.config.Configuration;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.component.aia.itpf.common.event.handler.EventSubscriber;
import com.ericsson.component.aia.mediation.parsers.exception.ParsingFailedException;
import com.ericsson.component.aia.mediation.parsers.parser.StreamParser;
import com.ericsson.component.aia.services.eps.EpsEngineConstants;
import com.ericsson.component.aia.services.eps.statistics.EpsStatisticsRegister;

/**
 * JUnit test class for {@link com.ericsson.component.aia.services.exteps.eh.parser.ModelDrivenPmRecordStreamParser}
 */
public class ModelDrivenPmRecordStreamParserTest {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String EMPTY_VARIABLE_TYPE = "";
    private static final String NULL_VARIABLE_TYPE = null;
    private static final String VALID_SCHEMA_CHECK_INTERVAL = "1000";
    private static final String INVALID_SCHEMA_CHECK_INTERVAL = "xxx";
    private static final String VALID_MODELS_FILEPATH = "test/";
    private static final String VALID_SCHEMA_TYPE = "CELLTRACE";
    private static final String VALID_SUBNETWORK = "Dublin";
    private static final String VALID_UTCOFFSET = "+00.00";
    private static final String VALID_DECODED_TYPE = "pojo";

    private final EventSubscriber mockedSubscriber = mock(EventSubscriber.class);

    private EventHandlerContext createContext(final String schemaType, final String subnetwork, final String utcOffset, final String decodedEventType,
                                              final String modelFilepath, final String schemaCheckIntervalString) {

        final Configuration mockConfig = mock(Configuration.class);
        when(mockConfig.getStringProperty(ModelDrivenPmRecordStreamParser.SCHEMA_TYPE)).thenReturn(schemaType);
        when(mockConfig.getStringProperty(ModelDrivenPmRecordStreamParser.SUBNETWORK)).thenReturn(subnetwork);
        when(mockConfig.getStringProperty(ModelDrivenPmRecordStreamParser.UTC_OFFSET)).thenReturn(utcOffset);
        when(mockConfig.getStringProperty(ModelDrivenPmRecordStreamParser.DECODER_TYPE)).thenReturn(decodedEventType);
        when(mockConfig.getStringProperty(ModelDrivenPmRecordStreamParser.MODELS_FILEPATH)).thenReturn(modelFilepath);
        when(mockConfig.getStringProperty(ModelDrivenPmRecordStreamParser.SCHEMA_CHECK_INTERVAL)).thenReturn(schemaCheckIntervalString);

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

    @Test(expected = IllegalStateException.class)
    public void init_modelFilepathNotSet_IllegalStateException() {
        final ModelDrivenPmRecordStreamParser handler = new ModelDrivenPmRecordStreamParser();
        handler.init(createContext(VALID_SCHEMA_TYPE, VALID_SUBNETWORK, VALID_UTCOFFSET, VALID_DECODED_TYPE, EMPTY_VARIABLE_TYPE,
                VALID_SCHEMA_CHECK_INTERVAL));
    }

    @Test(expected = IllegalStateException.class)
    public void init_modelFilepathNull_IllegalStateException() {
        final ModelDrivenPmRecordStreamParser handler = new ModelDrivenPmRecordStreamParser();
        handler.init(createContext(VALID_SCHEMA_TYPE, VALID_SUBNETWORK, VALID_UTCOFFSET, VALID_DECODED_TYPE, NULL_VARIABLE_TYPE,
                VALID_SCHEMA_CHECK_INTERVAL));
    }

    @Test(expected = IllegalStateException.class)
    public void init_schemaCheckIntervalStringNotSet_IllegalStateException() {
        final ModelDrivenPmRecordStreamParser handler = new ModelDrivenPmRecordStreamParser();
        handler.init(
                createContext(VALID_SCHEMA_TYPE, VALID_SUBNETWORK, VALID_UTCOFFSET, VALID_DECODED_TYPE, VALID_MODELS_FILEPATH, EMPTY_VARIABLE_TYPE));
    }

    @Test(expected = IllegalStateException.class)
    public void init_schemaCheckIntervalStringNull_IllegalStateException() {
        final ModelDrivenPmRecordStreamParser handler = new ModelDrivenPmRecordStreamParser();
        handler.init(
                createContext(VALID_SCHEMA_TYPE, VALID_SUBNETWORK, VALID_UTCOFFSET, VALID_DECODED_TYPE, VALID_MODELS_FILEPATH, NULL_VARIABLE_TYPE));
    }

    @Test
    public void init_schemaCheckIntervalStringNotNumber_DefaultSchemaCheckIntervalUsed() {
        final ModelDrivenPmRecordStreamParser handler = new ModelDrivenPmRecordStreamParser();
        handler.init(createContext(VALID_SCHEMA_TYPE, VALID_SUBNETWORK, VALID_UTCOFFSET, VALID_DECODED_TYPE, VALID_MODELS_FILEPATH,
                INVALID_SCHEMA_CHECK_INTERVAL));
        assertEquals(60000, handler.getSchemaCheckInterval());
    }

    @Test
    public void getParser_validParameters_returnsStreamParser() throws ParsingFailedException {
        final ModelDrivenPmRecordStreamParser handler = new ModelDrivenPmRecordStreamParser();
        handler.init(createContext(VALID_SCHEMA_TYPE, VALID_SUBNETWORK, VALID_UTCOFFSET, VALID_DECODED_TYPE, VALID_MODELS_FILEPATH,
                VALID_SCHEMA_CHECK_INTERVAL));
        assertTrue(handler.getParser() instanceof StreamParser);
    }

}
