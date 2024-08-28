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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the Queue Processor.
 * This class will receive objects and add them to an internal BlockingQueue.
 * Once the BlockingQueue is full, any new events received will be dropped and logged
 */
class QueueProcessorThread implements Runnable, IQueueProcessorThread {

    private static final int DROPPED_RECORD_THRESHOLD = 1000;

    private static final Logger logger = LoggerFactory.getLogger(QueueProcessorThread.class);

    private final BlockingQueue<Object> objectQueue;

    private final SingleThreadQueueHandler singleThreadQueueHandler;

    private int dropCount;

    private final Thread currentThread;

    private boolean isActive = true;

    /**
     * Constructs a new QueueProcessorThread of given size, with the given name for the given SingleThreadQueueHandler
     * @param queueSize Maximum size of BlockingQueue
     * @param threadName Name of thread, to appear in logging messages and metrics
     * @param singleThreadQueueHandler to notify of dropped events
     */
    QueueProcessorThread(final int queueSize, final String threadName, final SingleThreadQueueHandler singleThreadQueueHandler) {
        this.dropCount = 0;
        this.objectQueue = new LinkedBlockingQueue<>(queueSize);
        this.singleThreadQueueHandler = singleThreadQueueHandler;

        logger.debug("Starting QueueProcessorThread ... ");
        this.currentThread = new Thread(this);
        currentThread.setName(threadName);
        currentThread.start();
    }

    @Override
    public void run() {
        while (isActive) {
            try {
                final Object object = objectQueue.take();
                singleThreadQueueHandler.sendEvent(object);
            } catch (final InterruptedException interruptedException) {
                logger.warn("{} thread was interrupted", currentThread.getName());
            } catch (final Exception exception) {
                logger.error("Unexpected error occurred", exception);
            }
        }
        logger.warn("Stopping '{}' thread ...", currentThread.getName());
    }

    @Override
    public void process(final Object object) {
        logger.trace("putting object {} in queue to process", object.getClass());
        final boolean isObjectAdded = objectQueue.offer(object);
        if (!isObjectAdded) {
            logger.trace("Unable to add {} to Queue Dropping record ", object.getClass().getSimpleName());
            singleThreadQueueHandler.incrDroppedEventsIfStatisticsOn(1);
            logAndIncrementDropRecords();
        }
    }

    private void logAndIncrementDropRecords() {
        if (dropCount >= DROPPED_RECORD_THRESHOLD) {
            logger.error("Unable to add {} records to Queue serviced by the thread named \"{}\", Dropped {} records ", dropCount,
                    currentThread.getName(), dropCount);
            dropCount = 0;
        }
        dropCount += 1;
    }

    @Override
    public void destroy() {
        logger.info("Stopping {} thread  . . .", currentThread.getName());
        isActive = false;
    }

    @Override
    public long getQueueSize() {
        return objectQueue.size();
    }

}