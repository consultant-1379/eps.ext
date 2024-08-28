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

package com.ericsson.component.aia.services.exteps.io.adapter.streaming;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.codahale.metrics.Meter;
import com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler;
import com.ericsson.component.aia.itpf.common.io.OutputAdapter;
import com.ericsson.component.aia.services.eps.EpsEngineConstants;
import com.ericsson.component.aia.services.eps.statistics.EpsStatisticsRegister;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.constants.Constants;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.constants.StreamType;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.exceptions.ClientNotConnectedException;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.util.StreamingPropertyValidator;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.util.SystemPropertyRetriever;

public class StreamingOutputAdapter extends AbstractEventHandler implements OutputAdapter {

    private static final int SLEEP_BETWEEN_RETRIES = 10000;
    private static final String URI = "streaming:/";
    private StreamingConfig config;
    private Meter eventMeter;
    private EpsStatisticsRegister statisticsRegister;
    private SocketChannel socketChannel;
    private SocketAddress destAddr;
    private boolean hasValidConfiguration;

    @Override
    protected void doInit() {
        final StreamType streamType = new StreamType(this.getConfiguration().getStringProperty(Constants.STREAM_TYPE));
        config = buildConfig(streamType.getSuffixString());
        if (hasValidConfiguration) {
            destAddr = new InetSocketAddress(config.getHost(), config.getPort());
            tryConnectToClient();
        }
        initialiseStatistics(streamType.getSuffixString());
    }

    protected StreamingConfig buildConfig(final String suffixString) {
        final StreamingConfig config = getOutputAdapterConfig();
        try {
            config.setHost(SystemPropertyRetriever.retrieveStringSystemProperty(Constants.OUTPUT_IP + suffixString));
            config.setPort(SystemPropertyRetriever.retrieveIntegerSystemProperty(Constants.OUTPUT_PORT + suffixString));
            hasValidConfiguration = true;
        } catch (IllegalArgumentException e) {
            log.error("Could not find the value of configuration property output ip and port {}", e.getMessage());
        }
        return config;
    }

    protected StreamingConfig getOutputAdapterConfig() {
        return StreamingPropertyValidator.validate(getEventHandlerContext().getEventHandlerConfiguration().getAllProperties(), this.isStatisticsOn());
    }

    private boolean isStatisticsOn() {
        return (statisticsRegister != null) && statisticsRegister.isStatisticsOn();
    }

    protected void tryConnectToClient() {
        try {
            connectWithRetries(getConnectionRetries());
        } catch (InterruptedException | ClientNotConnectedException e) {
            log.error("Could not connect to client running on " + config.getHost() + ":" + config.getPort(), e);
        }
    }

    protected int getConnectionRetries() {
        return config.getConnectionRetries();
    }

    protected void connectWithRetries(final int retries) throws ClientNotConnectedException, InterruptedException {
        if (retries == 0) {
            throw new ClientNotConnectedException("Max connection attempts exceeded");
        }
        log.info("Trying to connect to client...");
        try {
            socketChannel = getNewSocket();
            socketChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, config.getConnectionKeepAlive());
            socketChannel.configureBlocking(true);
            socketChannel.socket().connect(destAddr, SLEEP_BETWEEN_RETRIES);
            if (socketChannel.finishConnect()) {
                log.info("Successfully connected to client running on {}:{}.", config.getHost(), config.getPort());
            } else {
                log.debug("Socket not connected, retrying...");
                Thread.sleep(getTimeBetweenRetries());
                connectWithRetries(retries - 1);
            }
        } catch (final IOException e) {
            log.error("IOException seen while trying to conect, retrying... {}", e.getMessage());
            Thread.sleep(getTimeBetweenRetries());
            connectWithRetries(retries - 1);
        }
    }

    protected int getTimeBetweenRetries() {
        return SLEEP_BETWEEN_RETRIES;
    }

    protected SocketChannel getNewSocket() throws IOException {
        return SocketChannel.open();
    }

    private void initialiseStatistics(final String suffixString) {
        statisticsRegister = (EpsStatisticsRegister) getEventHandlerContext()
                .getContextualData(EpsEngineConstants.STATISTICS_REGISTER_CONTEXTUAL_DATA_NAME);
        if (statisticsRegister == null) {
            log.error("statisticsRegister should not be null. Event metrics will not be recorded.");
        } else {
            createMetrics(suffixString);
        }
    }

    private void createMetrics(final String suffixString) {
        if (statisticsRegister.isStatisticsOn()) {
            eventMeter = statisticsRegister.createMeter(StreamingOutputAdapter.class.getSimpleName() + "@eventsReceived" + suffixString);
        }
    }

    @Override
    public void onEvent(final Object inputEvent) {
        if (hasValidConfiguration) {
            trySendEvent(inputEvent);
        }
    }

    protected void trySendEvent(final Object inputEvent) {
        final byte[] eventAsBytes = (byte[]) inputEvent;
        final ByteBuffer buffer = ByteBuffer.allocate(eventAsBytes.length);
        buffer.put(eventAsBytes);
        buffer.flip();
        try {
            writeEvent(buffer);
        } catch (final IOException e) {
            log.error("No connection with client.");
            reconnect();
            try {
                writeEvent(buffer);
            } catch (final IOException ex) {
                log.error("Failed to send event to client.");
            }
        }
    }

    private void writeEvent(final ByteBuffer buffer) throws IOException {
        socketChannel.write(buffer);

        while (buffer.hasRemaining()) {
            try {
                log.debug("The tcp buffer is full, time to slow down");
                Thread.sleep(50);
            } catch (final InterruptedException e) {
                log.error("InterruptedException while trying to write data.", e);
            }
            socketChannel.write(buffer);
        }
        recordMetric();
    }

    private void recordMetric() {
        if (isStatisticsOn()) {
            eventMeter.mark();
        }
    }

    @Override
    public void destroy() {
        closeSocketChannel();
    }

    private void reconnect() {
        closeSocketChannel();
        tryConnectToClient();
    }

    @Override
    public boolean understandsURI(final String uri) {
        return uri != null && uri.equals(URI);
    }

    public Meter getEventMeter() {
        return eventMeter;
    }

    protected SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public StreamingConfig getConfig() {
        return config;
    }

    /*
     * Closing the scoket channel
     */
    private void closeSocketChannel() {
        if (socketChannel != null) {
            try {
                socketChannel.close();
            } catch (final IOException e) {
                log.error("IOException while closing the socket Channel", e);
            }
        }
    }

}