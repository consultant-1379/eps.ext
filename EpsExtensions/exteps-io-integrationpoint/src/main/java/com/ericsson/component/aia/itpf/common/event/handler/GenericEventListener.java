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
package com.ericsson.component.aia.itpf.common.event.handler;

import static com.ericsson.component.aia.services.exteps.io.adapter.streaming.constants.Constants.STREAM_TYPE;

import com.ericsson.component.aia.services.exteps.io.adapter.streaming.constants.StreamType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.aia.ipl.EventListener;
import com.ericsson.component.aia.services.exteps.io.common.statistics.EpsExtStatisticsHelper;

/**
 * The listener interface for receiving events. The class that is interested in processing an event implements this interface, and the object created
 * with that class is registered with a component using the component's <code>registerEventListener<code> method. When the event occurs, that object's
 * appropriate method is invoked.
 *
 * @param <V>
 *            the event type
 */
public class GenericEventListener<V> implements EventListener<V> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericEventListener.class);

    private final AbstractEventHandler handler;

    private EpsExtStatisticsHelper epsExtStatisticsHelper;

    /**
     * Instantiates a new generic event listener. A handler is required since it needs pass the event is receives to the next handler in the flow
     *
     * @param handler
     *            the handler
     */
    public GenericEventListener(final AbstractEventHandler handler) {
        this.handler = handler;
        epsExtStatisticsHelper = new EpsExtStatisticsHelper(this.getClass().getSimpleName());
        StreamType streamType = new StreamType(handler.getConfiguration().getStringProperty(STREAM_TYPE));
        epsExtStatisticsHelper.setMetricsSuffix(streamType.getSuffixString());
        epsExtStatisticsHelper.initialiseStatistics(handler.getEventHandlerContext());
    }

    @Override
    public void onEvent(final V event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Received an event: {}", String.valueOf(event));
        }
        handler.sendToAllSubscribers(event);

        if (epsExtStatisticsHelper.isStatisticsOn()) {
            epsExtStatisticsHelper.mark();
        }
    }

}
