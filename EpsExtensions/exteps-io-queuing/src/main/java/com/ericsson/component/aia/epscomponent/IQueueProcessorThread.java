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

/**
 * Interface to QueueProcessorThread.
 */
interface IQueueProcessorThread {

    /**
     * Add object to queue, or drop if queue is full
     * @param object The object to add to queue
     */
    void process(final Object object);

    /**
     * Destroy queue and drop any objects that are received after destroy is called
     */
    void destroy();

    /**
     * Get current size of queue
     * @return current number of objects in the queue
     */
    long getQueueSize();

}