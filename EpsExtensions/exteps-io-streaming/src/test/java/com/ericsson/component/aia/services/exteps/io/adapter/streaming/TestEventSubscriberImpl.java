/*------------------------------------------------------------------------------
 *******************************************************************************
 * © Ericsson AB 2013-2015 - All Rights Reserved
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.exteps.io.adapter.streaming;

import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;
import com.ericsson.component.aia.itpf.common.event.handler.EventSubscriber;

/**
 * EventSubscriber implementation helper class for ReactiveStreamingInputAdapterTest test
 */
public class TestEventSubscriberImpl implements EventSubscriber {

    private final String id;
    private final EventInputHandler handler;

    public TestEventSubscriberImpl(final String id, final EventInputHandler handler) {
        this.id = id;
        this.handler = handler;
    }

    @Override
    public String getIdentifier() {
        return id;
    }

    @Override
    public void sendEvent(final Object event) {
        handler.onEvent(event);
    }

}