/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.exteps.io.adapter.ipl;

import static com.ericsson.component.aia.services.exteps.io.adapter.common.Constants.*;


import org.apache.avro.generic.GenericRecord;

import com.ericsson.aia.ipl.EventPublisher;
import com.ericsson.aia.ipl.factory.EventServiceFactory;
import com.ericsson.component.aia.itpf.common.config.Configuration;
import com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler;
import com.ericsson.component.aia.itpf.common.io.OutputAdapter;
import com.ericsson.component.aia.services.exteps.io.common.statistics.EpsExtStatisticsHelper;
import com.ericsson.component.aia.services.exteps.io.adapter.util.GlobalPropertiesRetrieverUtil;
/**
 * The Class AvroOutputAdapter, responsible for publishing Avro GenericRecords using IPL.
 */
public class AvroOutputAdapter extends AbstractEventHandler implements OutputAdapter {

    public static final String URI = "avro:/";
    private static final String EVENT_ID_FIELD = "_ID";
    private EventPublisher<GenericRecord> publisher;
    private EpsExtStatisticsHelper epsExtStatisticsHelper;

    @Override
    public void doInit() {
        final String integrationPointUri = getConfig().getStringProperty(INTEGRATION_POINT_URI);
        final String integrationPointName = getConfig().getStringProperty(INTEGRATION_POINT_NAME);
        final String globalPropertiesFilePath = GlobalPropertiesRetrieverUtil.getFilePath(getConfig());
        final EventServiceFactory<GenericRecord> factory = new EventServiceFactory<>(integrationPointUri, globalPropertiesFilePath);
        this.publisher = factory.createEventPublisher(integrationPointName);
        epsExtStatisticsHelper = new EpsExtStatisticsHelper(this.getClass().getSimpleName());
        epsExtStatisticsHelper.initialiseStatistics(getEventHandlerContext());
    }

    @Override
    public void onEvent(final Object inputEvent) {
        checkInputEvent(inputEvent);
        sendRecord((GenericRecord) inputEvent);
        if (epsExtStatisticsHelper.isStatisticsOn()) {
            epsExtStatisticsHelper.mark();
        }
    }

    private void checkInputEvent(final Object inputEvent) {
        if (inputEvent == null) {
            throw new IllegalArgumentException("Input event must not be null");
        }
        final boolean correctEventType = inputEvent instanceof GenericRecord;
        if (!correctEventType) {
            throw new IllegalArgumentException("Input event must be a type of GenericRecord");
        }
    }

    /**
     * Publishes the generic record.
     *
     * @param record
     *            the generic record
     */
    protected void sendRecord(final GenericRecord record) {
        publisher.sendRecord(String.valueOf(record.get(EVENT_ID_FIELD)), record);
    }

    @Override
    public boolean understandsURI(final String uri) {
        return uri != null && uri.startsWith(URI);
    }

    public EventPublisher<GenericRecord> getPublisher() {
        return publisher;
    }

    public Configuration getConfig() {
        return getConfiguration();
    }

    public EpsExtStatisticsHelper getEpsExtStatisticsHelper() {
        return epsExtStatisticsHelper;
    }

    @Override
    public void destroy() {
        log.info("Shutting down {} ...", this.getClass().getCanonicalName());
        try {
            if (publisher != null) {
                publisher.close();
            }
        } catch (final Exception exception) {
            log.error("Unexpected exception while closing publisher", exception);
        }
    }
}
