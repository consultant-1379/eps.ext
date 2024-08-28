/**
 *
 * (C) Copyright LM Ericsson System Expertise AT/LMI, 2016
 *
 * The copyright to the computer program(s) herein is the property of Ericsson  System Expertise EEI, Sweden.
 * The program(s) may be used and/or copied only with the written permission from Ericsson System Expertise
 * AT/LMI or in  * accordance with the terms and conditions stipulated in the agreement/contract under which
 * the program(s) have been supplied.
 *
 */
package com.ericsson.component.aia.services.exteps.ioadapter.dfconsumer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.common.transport.service.GenericEventListener;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.component.aia.itpf.common.event.handler.EventSubscriber;
import com.ericsson.component.aia.services.exteps.ioadapter.builder.IO_CONSTANTS;

import static  com.google.common.base.Preconditions.checkState;
import static  com.google.common.base.Preconditions.checkArgument;
/**
 * @author eachsaj
 * Apr 25, 2016
 */
public class EPSMessageListener<V> implements GenericEventListener<V> {

    private static final Logger logger = LoggerFactory.getLogger(EPSMessageListener.class);
    // temporary events holder
    private final BlockingQueue<Collection<V>> queue = new LinkedBlockingQueue<Collection<V>>();

    /**
     * Message Processors manager
     */
    private ExecutorService exec;

     /**
     * Number of data processors
     */
    private int numberProcessors;

    // workers
    private List<DataHandler<V>> workers = new ArrayList<>();

    // call back to event listeners
    private EventHandlerContext eventHandlerContext;

    private Properties properties;

    /**
     * @return the queue
     */
    public BlockingQueue<Collection<V>> getQueue() {
        return queue;
    }
    /**
     * @return the numberProcessors
     */
    public int getNumberProcessors() {
        return numberProcessors;
    }
    /**
     * @return the eventHandlerContext
     */
    public EventHandlerContext getEventHandlerContext() {
        return eventHandlerContext;
    }
    /**
     * @return the properties
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * A method to initialize the message listener class
     * @param props contains the configuration information from FF
     * @param eventHandlerContext is the EPS {@link EventHandlerContext} instance
     */
    public void init(Properties props, EventHandlerContext eventHandlerContext) {
        logger.trace("init( {} , {} ) -->", props.toString(), eventHandlerContext.getEventSubscribers());
        checkArgument(props != null, "Input adapter not initialized properly.");
        checkArgument(eventHandlerContext != null , "Event Handler Context cannot be null.");
        this.numberProcessors =  Integer.valueOf(props.getProperty(IO_CONSTANTS.INPUT_THREAD_POOL_SIZE_PARAM, "5"));;
        this.eventHandlerContext = eventHandlerContext;
        this.properties = props;
        exec = Executors.newFixedThreadPool(numberProcessors);
        for (int pCounter  = 0; pCounter  < numberProcessors; pCounter ++) {
            DataHandler<V> runner = new DataHandler<V>(queue, eventHandlerContext);
            workers.add(runner);
            exec.submit(runner);
        }

        logger.trace("init({},{},) <--");
    }
    /**
     * To stop all the workers associated with it.
     *
     *   */
    public void stop() {
        checkState(exec != null, "Thread process manager not initialized");
        checkState(workers != null, "Worker list  not initialized");
        for (DataHandler<V> worker : workers) {
            worker.stop();
        }
        exec.shutdownNow();
    }
    @Override
    public void onEvent(Collection<V> events) {

        try {
            if (events != null) {
                if (events != null && events.size() > 0) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Adding  {} events to the processing queue");
                    }
                    if (logger.isTraceEnabled()) {
                        long freeMemory = Runtime.getRuntime().freeMemory() / 1048576;
                        logger.trace("Process-ID = {}  - free memory in MB = {}" , Thread.currentThread().getName(), freeMemory);
                    }

                    final long startTime = System.currentTimeMillis();
                    queue.add(events);
                    if (logger.isTraceEnabled()) {
                        final String tName = Thread.currentThread().getName();
                        logger.trace( "{} Local QSize =  {}", tName, queue.size());
                    }
                    final long duration = System.currentTimeMillis() - startTime;


                    if (logger.isDebugEnabled()) {
                        logger.debug( "{} events sent to eps Local queue size = {}. Queue.add Time : {}", events.size(), queue.size(), duration);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to forward the events to the correlation: ", e);
        }

    }


}

/**
A worker class for data handling
 * @param <V>
 */
class DataHandler<V> implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(EPSMessageListener.class);
    /**
     * data queue
     */
    private final BlockingQueue<Collection<V>> queue ;
    /**
     * reference to eps input adapater. it is bad design
     */
    private final EventHandlerContext eventHandlerContext;
    private boolean stop = false;
    DataHandler(BlockingQueue<Collection<V>> queue, EventHandlerContext eventHandlerContext) {
        logger.trace("DataHandler() -->");
        checkArgument(queue != null, " Event queue is not initialized");
        checkArgument(queue != null, " EventHandlerContext  is not initialized");
        this.queue = queue;
        this.eventHandlerContext = eventHandlerContext;
        logger.trace("DataHandler() -->");
    }

    public void stop() {
        // to do graceful shutdown
        stop = true;
        logger.info("DataHandler Stop called.");
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        logger.debug("DataHandler initialized.");
        while (!stop) {
            // if no topic available report warning and clean the queue .

            try {
                final Collection<V> event = queue.take();
                if (event != null) {
                    for (EventSubscriber listenerEach : this.eventHandlerContext.getEventSubscribers()) {
                        //This is because of limitation in the rawConverter utility that needs to be fixed.
                        for (V v : event) {
                            if (v != null) {
                                listenerEach.sendEvent(v);
                            }
                        }
                    }
                    logger.trace("{} Events forwarding to listeners . ExecutorService , Executor Q size: {}" , event.size(), queue.size());
                }
            } catch (InterruptedException e) {
                logger.warn("BatchProcessor thread interrupted, Reason {}" , e.getMessage());
                continue;
            } catch (Throwable t) {
                logger.error("Fatal error while forwarding events for {}", t.getMessage());
                continue;
            }
        }
        logger.info("DataHandler Stopped.");
    }
}
