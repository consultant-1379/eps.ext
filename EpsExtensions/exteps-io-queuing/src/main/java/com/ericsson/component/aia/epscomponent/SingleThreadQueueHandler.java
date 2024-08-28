/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.epscomponent;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler;
import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;
import com.ericsson.component.aia.services.exteps.io.common.statistics.EpsExtStatisticsHelper;

/**
 * Entry point of the Blocking Queue implementation, specified in flow file
 * Will receive events (as part of flow) and put into a queue.
 * Once Events are removed from the queue, they are sent to all subscribers, ie next step in flow
 */
public class SingleThreadQueueHandler extends AbstractEventHandler implements EventInputHandler {
    private static final String THREAD_QUEUE_SIZE = "threadQueueNumEntries";

    private static final String THREAD_QUEUE_DROPPED_RECORDS = "ThreadQueueDroppedRecords";

    private static final String APEPS = "apeps";

    private Gauge<Long> queueSizeGauge;

    private Counter droppedEvents;

    private String threadName;

    private IQueueProcessorThread queueProcessor;

    private EpsExtStatisticsHelper epsExtStatisticsHelper;

    @Override
    public void onEvent(final Object object) {
        log.trace("Object received {}, putting object in thread queue ", object.getClass());
        queueProcessor.process(object);
    }

    @Override
    protected void doInit() {
        log.trace("Initializing SingleThreadQueueHandler ...");
        int queueSize = 100;
        try {
            queueSize = Integer.parseInt(getConfiguration().getStringProperty("threadqueuesize").trim());
        } catch (final NumberFormatException exception) {
            log.error("Invalid queue size, queue size should be an integer, initializing queue with size {}", queueSize, exception);
        }
        threadName = getConfiguration().getStringProperty("threadname").trim();
        queueProcessor = new QueueProcessorThread(queueSize, threadName, this);
        epsExtStatisticsHelper = new SingleThreadQueueStatistics(this.getClass().getSimpleName());
        epsExtStatisticsHelper.initialiseStatistics(getEventHandlerContext());
    }

    /**
     * Send the specified object to the next step(s) in the flow
     * @param object to send to next step in flow
     */
    public void sendEvent(final Object object) {
        log.trace("Sending Object {}, to all subscriber ", object.getClass());
        sendToAllSubscribers(object);
    }

    /**
     * Checks to see if statistics reporting is enabled and if so, increments the metric for dropped events
     * @param count Number of events that were dropped
     */
    void incrDroppedEventsIfStatisticsOn(final int count) {
        if (epsExtStatisticsHelper.isStatisticsOn()) {
            droppedEvents.inc(count);
            if (droppedEvents.getCount() < 0) {
                droppedEvents.dec(droppedEvents.getCount());
                droppedEvents.inc(count);
            }
        }
    }

    @Override
    public void destroy() {
        queueProcessor.destroy();
        queueProcessor = new NullPatternQueueProcessorThread();
    }

    private class NullPatternQueueProcessorThread implements IQueueProcessorThread {

        boolean hasLogged;

        @Override
        public void process(final Object object) {
            if (!hasLogged) {
                log.info("Dropping object {} because shut down has been requested", object.getClass());
                hasLogged = true;
            }
        }

        @Override
        public void destroy() {
        }

        @Override
        public long getQueueSize() {
            return 0;
        }
    }
    private class SingleThreadQueueStatistics extends EpsExtStatisticsHelper {

        SingleThreadQueueStatistics(String clazzName) {
            super(clazzName);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void registerStatisticsMetrics() {
            registerQueueSizeGauge();
            createDroppedRecordsCounter();
        }
        private void registerQueueSizeGauge() {
            queueSizeGauge = new Gauge<Long>() {

                @Override
                public Long getValue() {
                    return queueProcessor.getQueueSize();
                }
            };
            epsStatisticsRegister.registerGuage(APEPS + "." + threadName + THREAD_QUEUE_SIZE, queueSizeGauge);
        }
        private void createDroppedRecordsCounter() {
            droppedEvents = epsStatisticsRegister.createCounter(APEPS + "." + threadName + THREAD_QUEUE_DROPPED_RECORDS);
        }
    }
}