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
package com.ericsson.component.aia.itpf.common.event.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.aia.ipl.EventCollectionListener;
import com.ericsson.aia.ipl.batch.BatchEvent;
import com.ericsson.aia.ipl.batch.RecordWrapper;
import com.ericsson.component.aia.services.exteps.io.common.statistics.EpsExtStatisticsHelper;

/**
 * The listener interface for receiving events. The class that is interested in processing an event implements this interface, and the object created
 * with that class is registered with a component using the component's <code>registerEventListener<code> method. When the event occurs, that object's
 * appropriate method is invoked.
 *
 * @param <V>
 *            the event type
 */
public class BatchEventListener<V> implements EventCollectionListener<V> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchEventListener.class);

    private final AbstractEventHandler handler;

    private final EpsExtStatisticsHelper epsExtStatisticsHelper;

    /**
     * Instantiates a new generic event listener. A handler is required since it needs pass the event is receives to the next handler in the flow
     *
     * @param handler
     *            the handler
     */
    public BatchEventListener(final AbstractEventHandler handler) {
        this.handler = handler;
        this.epsExtStatisticsHelper = new EpsExtStatisticsHelper(this.getClass().getSimpleName());
        this.epsExtStatisticsHelper.initialiseStatistics(handler.getEventHandlerContext());
    }

    @Override
    public void onEvent(final V event) {
        LOGGER.debug("Received an event: {}", String.valueOf(event));
        handler.sendToAllSubscribers(event);
        if (epsExtStatisticsHelper.isStatisticsOn()) {
            epsExtStatisticsHelper.mark();
        }
    }

    @Override
    public void onEvent(final RecordWrapper<V> event) {
        LOGGER.debug("Received an event: {}", event);
        handler.sendToAllSubscribers(event);
        if (epsExtStatisticsHelper.isStatisticsOn()) {
            epsExtStatisticsHelper.mark();
        }
    }

    @Override
    public void batchEvent(final BatchEvent batchEvent) {
        LOGGER.debug("Received batch event: {}", batchEvent);
        handler.sendToAllSubscribers(batchEvent);
    }

}
