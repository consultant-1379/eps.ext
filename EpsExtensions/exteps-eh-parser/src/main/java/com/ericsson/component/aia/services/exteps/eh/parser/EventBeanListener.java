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
package com.ericsson.component.aia.services.exteps.eh.parser;

import com.ericsson.component.aia.itpf.common.event.handler.AbstractApplicationEventHandler;
import com.ericsson.component.aia.mediation.parsers.receiver.DecodedEventReceiver;

/**
 * This class listens for {@link com.ericsson.oss.mediation.parsersapi.eventbean.EventBean}
 */
public class EventBeanListener implements DecodedEventReceiver {

    private final AbstractApplicationEventHandler parser;

    /**
     * Constructor
     * @param parser parser
     */
    public EventBeanListener(final AbstractApplicationEventHandler parser) {
        this.parser = parser;
    }

    /**
     *
     * @param myEventBeanParam
     */
    @Override
    public void decodedEventPublisher(final Object myEventBeanParam) {
        parser.sendEvent(myEventBeanParam);

    }
}
