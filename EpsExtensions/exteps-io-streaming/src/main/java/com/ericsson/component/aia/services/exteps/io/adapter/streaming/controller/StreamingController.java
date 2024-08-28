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

package com.ericsson.component.aia.services.exteps.io.adapter.streaming.controller;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.mediation.netty.*;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.StreamingConfig;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.config.StreamingConfigurationProvider;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.config.StreamingDatapathProvider;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.listener.RecordListener;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.listener.StreamingListener;

/**
 * This class is responsible for controlling a PM Streaming client.
 *
 * @since 1.0.7
 *
 */
public class StreamingController implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(StreamingController.class);
    private static final long STATE_TRANSITION_DELAY = 10000;
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    private Engine engine;
    private String datapathName = "StreamController_";
    private StreamingState streamingState = StreamingState.Disconnected;
    private StreamingConfig config;
    private RecordListener listener = null;

    /**
     * Default constructor to JUnit
     */
    public StreamingController() {
    }

    /**
     * Constructor, initialize the PM streaming controller
     *
     * @param listener
     * @param config
     */
    public StreamingController(final RecordListener listener) {
        this.listener = listener;
        StreamingListener.setController(this);
        StreamingConfigurationProvider.setController(this);
        StreamingDatapathProvider.setController(this);
        executorService.execute(this);
    }

    /**
     * Start the PM streaming client, this runs the PM streaming client handling state machine
     */
    public void start(final StreamingConfig config) {
        logger.debug("Streaming client starting . . .");
        waitForStateWithTimeout(StreamingState.Disconnected);
        if (streamingState != StreamingState.Disconnected) {
            logger.error("Streaming state should be Disconnected but is {}. Continuing anyway", streamingState);
        }
        this.config = config;
        setDataPathName();
        streamingState = StreamingState.Connecting;
        logger.debug("DataPath {}: -->Connecting", datapathName);
    }

    /**
     * Resetting the PM streaming client.
     */
    public void reset() {
        if (streamingState == StreamingState.Connecting || streamingState == StreamingState.Connected) {
            logger.info("Ordering Streaming client reset. Will reset the datapath [{}]", datapathName);
            streamingState = StreamingState.Resetting;
            logger.debug("DataPath {}: -->Resetting", datapathName);
        } else {
            logger.warn("Reset was requested but the controller is not in Connected or Connecting state. State is: {}", streamingState);
        }
    }

    public void connected() {
        if (streamingState == StreamingState.Connecting) {
            streamingState = StreamingState.Connected;
            logger.debug("DataPath {}: -->Connected", datapathName);
        } else {
            logger.info("DataPath {}: Not set to status connected because it is not in state Connecting", datapathName);
        }

    }

    /**
     * Stops the PM streaming client.
     */
    public void stop() {
        logger.debug("Ordering Streaming [{}] client stop", datapathName);
        logger.debug("DataPath {}: -->Disconnecting", datapathName);
        streamingState = StreamingState.Disconnecting;
    }

    public void stopBlocking() {
        stop();
        waitForStateWithTimeout(StreamingState.Disconnected);
    }

    /**
     * Return the Client Config details.
     *
     * @return
     */
    public StreamingConfig getConfig() {
        return config;
    }

    private void setDataPathName() {
        datapathName = "StreamController_" + config.getHost() + ':' + config.getPort();
    }

    public RecordListener getStreamListener() {
        return listener;
    }

    /**
     * This method runs the Streaming state machine in a separate thread
     */
    @Override
    public void run() {
        while (true) {
            handleTransition();
        }
    }

    public Engine getNettyEngine() {
        return EngineFactory.getInstance();
    }

    public long getTransititionDelay() {
        return STATE_TRANSITION_DELAY;
    }

    public void handleTransition() {
        switch (streamingState) {
            case Connecting: {
                handleConnect();
                break;
            }
            case Connected: {
                logger.debug("DataPath {}: Connected", datapathName);
                break;
            }
            case Resetting: {
                handleReset();
                break;
            }
            case Disconnecting: {
                handleDisconnect();
                break;
            }
            case Disconnected: {
                break;
            }
            default: {
                logger.warn("DataPath {}: invalid state", datapathName);
                break;
            }
        }
        waitForTransition();
    }

    protected void waitForTransition() {
        try {
            Thread.sleep(getTransititionDelay());
        } catch (final InterruptedException e) {
            logger.debug("Streaming client interrupt ordered");
            streamingState = StreamingState.Disconnecting;
        }
    }

    protected void handleDisconnect() {
        logger.debug("DataPath {}: Disconnecting", datapathName);
        stopDataPathAndEngine();
        logger.debug("DataPath {}: -->Disconnected", datapathName);
        streamingState = StreamingState.Disconnected;
    }

    protected void handleReset() {
        logger.debug("DataPath {}: Resetting", datapathName);
        stopDataPathAndEngine();
        streamingState = StreamingState.Connecting;
        logger.debug("DataPath {}: -->Connecting", datapathName);
    }

    private void stopDataPathAndEngine() {
        engine = getNettyEngine();
        stopDataPath(engine);
        stopEngine(engine);
    }

    private void stopDataPath(final Engine engine) {
        try {
            logger.info("Stopping the datapath [{}]", datapathName);
            engine.stopDataPath(datapathName);
        } catch (final Exception exception) {
            logger.error("Exception thrown while stopping the dataPath [{}]", datapathName, exception);
        }
    }

    private void stopEngine(final Engine engine) {
        try {
            logger.info("Stopping the streaming netty engine");
            engine.stop();
        } catch (final Exception exception) {
            logger.error("Exception thrown while stopping the streaming netty engine", exception);
        }
    }

    protected void handleConnect() {
        logger.debug("DataPath {}: Connecting . . .", datapathName);
        logger.info("Starting netty streaming engine and dataPath [{}]", datapathName);
        engine = getNettyEngine();
        try {
            engine.start();
            engine.startDataPath(datapathName);
        } catch (final EngineException exception) {
            logger.error("Exception thrown while starting netty streaming engine or datapath [{}]", datapathName, exception);
            reset();
        }
    }

    public void setStreamingState(final StreamingState streamingState) {
        this.streamingState = streamingState;
    }

    public StreamingState getStreamingState() {
        return streamingState;
    }

    private void waitForStateWithTimeout(final StreamingState streamingState) {
        final long startTime = System.currentTimeMillis();
        while (this.streamingState != streamingState && System.currentTimeMillis() < startTime + (STATE_TRANSITION_DELAY * 2)) {
            waitMillis(100);
        }
    }

    private void waitMillis(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (final InterruptedException exception) {
            exception.printStackTrace();
        }
    }
}
