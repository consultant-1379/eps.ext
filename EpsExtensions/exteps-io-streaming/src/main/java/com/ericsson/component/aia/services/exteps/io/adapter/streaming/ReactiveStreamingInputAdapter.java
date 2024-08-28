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

import java.util.Map;

import com.codahale.metrics.Meter;
import com.ericsson.component.aia.itpf.common.Clustered;
import com.ericsson.component.aia.itpf.common.Monitorable;
import com.ericsson.component.aia.itpf.common.event.ControlEvent;
import com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler;
import com.ericsson.component.aia.itpf.common.io.InputAdapter;
import com.ericsson.component.aia.services.eps.EpsEngineConstants;
import com.ericsson.component.aia.services.eps.statistics.EpsStatisticsRegister;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.controller.StreamingController;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.controller.StreamingState;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.listener.RecordListener;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.util.StreamingPropertyValidator;

public class ReactiveStreamingInputAdapter extends AbstractEventHandler implements InputAdapter, RecordListener, Monitorable, Clustered {

    private static final String EPS_EXT = "epsExt";
    private static final String ADAPTIVE_STREAMING_URL = "adaptive-streaming:/";
    private EpsStatisticsRegister statisticsRegister;
    private boolean statisticsOn;
    private Meter eventMeter;
    private StreamingController controller;

    public ReactiveStreamingInputAdapter() {
    }

    /*
     * Enable unit test for adapter, allowing TORStreamingController mocked instances to be passed here.
     */
    ReactiveStreamingInputAdapter(final StreamingController controller) {
        this.controller = controller;
    }

    @Override
    protected void doInit() {
        initialiseStatistics();
    }

    /**
     * Initialise statistics.
     */
    protected void initialiseStatistics() {
        statisticsRegister = (EpsStatisticsRegister) getEventHandlerContext()
                .getContextualData(EpsEngineConstants.STATISTICS_REGISTER_CONTEXTUAL_DATA_NAME);
        if (statisticsRegister == null) {
            log.error("Failed to initialize EpsStatisticsRegister ...");
        } else {
            statisticsOn = statisticsRegister.isStatisticsOn();
            if (statisticsOn) {
                eventMeter = statisticsRegister.createMeter(EPS_EXT + "_" + ReactiveStreamingInputAdapter.class.getSimpleName() + "_eventsReceived");
            }
        }
    }

    @Override
    public boolean understandsURI(final String uri) {
        return uri != null && uri.startsWith(ADAPTIVE_STREAMING_URL);
    }

    @Override
    public void onEvent(final Object inputEvent) {
        throw new UnsupportedOperationException("Operation not supported. PM Streaming input adapter is always entry points on event chain!");
    }

    @Override
    public void destroy() {
        log.warn("Destroy is triggered");
        super.destroy();
        if (controller != null) {
            controller.stopBlocking();
            controller = null;
        }
    }

    @Override
    public void streamReceived(final Object streamRecord) {
        this.sendToAllSubscribers(streamRecord);
        if (statisticsOn) {
            eventMeter.mark();
        }
    }

    @Override
    public void react(final ControlEvent controlEvent) {
        this.connectToStreaming(controlEvent.getData());
    }

    @Override
    public Object getStatus() {
        if (eventMeter == null) {
            return null;
        }
        final double oneMinuteRate = eventMeter.getOneMinuteRate();
        if (log.isDebugEnabled()) {
            log.debug("ReactiveStreamingInputAdapter report last 1 minute traffic as {} event/second. ", oneMinuteRate);
        }
        return oneMinuteRate;
    }

    private void connectToStreaming(final Map<String, Object> properties) {
        try {
            this.startConnectionManager(this.getStreamingConfigFromProperties(properties));
        } catch (final Exception exception) {
            log.info("ReactiveStreamingInputAdapter dynamically configured parameters are invalid. Stopping Execution", exception);
            throw new IllegalArgumentException("ReactiveStreamingInputAdapter dynamically configured parameters are invalid. Stopping Execution",
                    exception);
        }

        log.info("ReactiveStreamingInputAdapter dynamically configured parameters are valid. Now (re)connecting to Streaming Node");
    }

    public StreamingController getStreamingController() {
        return controller;
    }

    protected StreamingConfig getStreamingConfigFromProperties(final Map<String, Object> properties) {
        return StreamingPropertyValidator.validateInputAdapter(properties, statisticsOn);
    }

    protected StreamingController getNewStreamingController() {
        return new StreamingController(this);
    }

    private void startConnectionManager(final StreamingConfig streamingConfig) {
        if (controller == null && streamingConfig == null) {
            log.info("Idle message received on start up, No action will be taken.");
        } else if (streamingConfig == null) {
            log.info("Disconnect message received, Disconnecting from Streaming Application");
            if (controller.getStreamingState() != StreamingState.Disconnected) {
                controller.stop();
            } else {
                log.info("Streaming Controller is already in state Disconnected. StreamingAdaptor will not perform any action.");
            }
        } else if (controller == null) {
            log.info("Connect message received on start up.");
            controller = this.getNewStreamingController();
            controller.start(streamingConfig);
        } else {
            log.info("Reconfiguring Streaming Adaptor and Re-Connecting to Streaming Application.");
            controller.stop();
            controller.start(streamingConfig);
        }
    }

}
