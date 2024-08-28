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

import com.codahale.metrics.Meter;
import com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler;
import com.ericsson.component.aia.itpf.common.io.InputAdapter;
import com.ericsson.component.aia.services.eps.EpsEngineConstants;
import com.ericsson.component.aia.services.eps.statistics.EpsStatisticsRegister;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.controller.StreamingController;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.listener.RecordListener;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.util.StreamingPropertyValidator;

public class StreamingInputAdapter extends AbstractEventHandler implements InputAdapter, RecordListener {

    public static final String URI = "streaming:/";

    protected EpsStatisticsRegister statisticsRegister;

    protected StreamingController controller;

    protected Meter eventMeter;

    protected StreamingConfig config;

    public StreamingInputAdapter() {
    }

    /*
     * Enable unit test for adapter, allowing TORStreamingController mocked instances to be passed here.
     */
    StreamingInputAdapter(final StreamingController controller) {
        this.controller = controller;
    }

    @Override
    protected void doInit() {
        config = getConfig();
        controller = getController();
        initialiseStatistics();
        controller.start(config);
    }

    /**
     * Initialise statistics.
     */
    protected void initialiseStatistics() {
        statisticsRegister = (EpsStatisticsRegister) getEventHandlerContext()
                .getContextualData(EpsEngineConstants.STATISTICS_REGISTER_CONTEXTUAL_DATA_NAME);
        if (statisticsRegister == null) {
            log.error("statisticsRegister should not be null");
        } else {
            if (statisticsRegister.isStatisticsOn()) {
                eventMeter = statisticsRegister.createMeter(StreamingInputAdapter.class.getSimpleName() + "@" + "eventsReceived");
            }
        }
    }

    protected StreamingController getController() {
        return new StreamingController(this);
    }

    protected StreamingConfig getConfig() {
        return StreamingPropertyValidator.validateInputAdapter(getEventHandlerContext().getEventHandlerConfiguration().getAllProperties(),
                this.isStatisticsOn());
    }

    @Override
    public boolean understandsURI(final String uri) {
        return uri != null && uri.equals(URI);
    }

    @Override
    public void onEvent(final Object inputEvent) {
        throw new UnsupportedOperationException("Operation not supported. PM Streaming input adapter is always entry points on event chain!");
    }

    @Override
    public void destroy() {
        controller.stop();
        controller = null;
    }

    @Override
    public void streamReceived(final Object streamRecord) {
        sendToAllSubscribers(streamRecord);
        if (isStatisticsOn()) {
            eventMeter.mark();
        }
    }

    protected boolean isStatisticsOn() {
        return (statisticsRegister != null) && statisticsRegister.isStatisticsOn();
    }
}
