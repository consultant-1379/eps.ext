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

package com.ericsson.component.aia.services.exteps.io.adapter.streaming.listener;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.mediation.netty.protocol.StreamOutProtocol;
import com.ericsson.component.aia.mediation.netty.protocol.StreamOutProtocol.EventType;
import com.ericsson.component.aia.mediation.netty.protocol.parser.AbstractMuxEvent;
import com.ericsson.component.aia.mediation.netty.protocol.parser.DefaultStreamOutProtocolParser;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.controller.StreamingController;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.handlers.*;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.util.*;

/**
 * The class handles PM streaming Protocol for event streaming.
 *
 * @since 1.0.7
 *
 *        Every event streamed out from the streaming solution will be wrapped in a StreamOut message.
 *
 *        Each message contains a header portion followed by the original event.
 *
 *        The header part will have a defined structure according to the message type.
 *
 *        The message type will be the 1st byte in the header.
 *
 *        -------------------------- StreamOut Header -------------------------
 *
 *        ----------------- Event type ------------ Source ID -----------------
 *
 *        ----------------- (1 byte) ------------ (3 bytes) -------------------
 *
 *
 *        Event type ::
 *
 *        1 byte length
 *
 *        Describes type of message the Streaming solution is sending to client.
 *
 *        Possible values are 0 to 4 as described below:
 *
 *        0: Event message.
 *
 *        1: Protocol initialization message.
 *
 *        2: Connection message.
 *
 *        3: Disconnection message.
 *
 *        4: Dropped events message.
 *
 *
 *        Source ID ::
 *
 *        3 bytes length
 *
 *        0 < source id < 16777216
 *
 *        identifies the NE that produced this event.
 *
 */

@ChannelHandler.Sharable
public class StreamingListener extends ChannelInboundHandlerAdapter implements Metrics {

    private static final Object NULL_OBJECT = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(StreamingListener.class);
    private static final Logger METRICLOGGER = LoggerFactory.getLogger(METRICS);
    private static boolean monitorOn;
    private static StreamingController controller;
    private transient EventMetrics receivedMetrics;

    private final transient Map<StreamOutProtocol.EventType, AbstractStreamHandler> handlersMap = new HashMap<StreamOutProtocol.EventType, AbstractStreamHandler>();

    /**
     * Constructor, initialize the PM streaming listener
     */
    public StreamingListener() {
        super();
        LOGGER.debug("StreamingListener({})->");
        monitorOn = controller.getConfig().isMonitorOn();
        if (monitorOn) {
            receivedMetrics = EventMetrics.getInstance();
            receivedMetrics.setStartTime(System.currentTimeMillis());
            receivedMetrics.setMonitorOn(monitorOn);
            new StreamLoadMonitor(this, controller.getConfig().getMonitorPeriod());
        }
        addHandlers();
        LOGGER.debug("StreamingListener({})<-");
    }

    /**
     * Set the PM streaming controller being used
     *
     * @param theController
     */
    public static void setController(final StreamingController theController) {
        controller = theController;
    }

    public static StreamingController getController() {
        return controller;
    }

    public void incrementRecords() {
        if (monitorOn) {
            receivedMetrics.incrementRecords();
        }
    }

    private void invalidRecords(final long incrementAmount) {
        if (monitorOn) {
            receivedMetrics.incrementInvalidRecords(incrementAmount);
        }
    }

    @Override
    public void outputMetrics() {
        if (monitorOn) {
            receivedMetrics.setToTime(System.currentTimeMillis());
            METRICLOGGER.info(" {} , {} ", StreamingListener.class.getSimpleName(), receivedMetrics.toString());
            receivedMetrics.setStartTime(System.currentTimeMillis());
        }
    }

    @Override
    public void channelInactive(final ChannelHandlerContext context) {
        context.fireChannelInactive();
        LOGGER.debug("PM Streaming channel disconnected[{}]");
        controller.reset();
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        ctx.fireChannelActive();
        controller.connected();
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext context, final Throwable cause) {
        LOGGER.error("PM Streaming exception thrown[{}]", cause.getMessage());
        controller.reset();
        context.close();
    }

    @Override
    public void channelRead(final ChannelHandlerContext context, final Object object) {
        final ByteBuf byteBuf = (ByteBuf) object;
        final AbstractMuxEvent event = getParser().parse(byteBuf);
        final StreamOutProtocol.EventType eventType = event.eventType();
        incrementRecords();
        try {
            final AbstractStreamHandler handler = getHandler(eventType);
            if (isNull(handler)) {
                invalidRecords(1);
                LOGGER.error("Unexpected event type received {} ", eventType);
            } else {
                handler.handle(event);
            }
        } catch (final Exception e) {
            LOGGER.error("Processing of event failed {} ", eventType);
        } finally {
            byteBuf.release();
        }

    }

    /**
     * @return Checks if the object is null , return {true|false}
     */
    private boolean isNull(final Object checkIfNull) {
        return checkIfNull == NULL_OBJECT;
    }

    /**
     * Add all Handlers to process incoming events.
     */
    private void addHandlers() {
        handlersMap.put(EventType.INIT, new InitilizationHandler());
        handlersMap.put(EventType.CONNECTION, new ConnectionHandler());
        handlersMap.put(EventType.PAYLOAD, new EventHandler());
        handlersMap.put(EventType.DROPPED, new DroppedEventHandler());
        handlersMap.put(EventType.DISCONNECTION, new DisConnectionHandler());
    }

    /**
     * @param eventType
     * @return Handler to process the events.
     */
    protected AbstractStreamHandler getHandler(final EventType eventType) {
        return handlersMap.get(eventType);
    }

    protected DefaultStreamOutProtocolParser getParser() {
        return new DefaultStreamOutProtocolParser();
    }

    public EventMetrics getReceivedMetrics() {
        return receivedMetrics;
    }
}
