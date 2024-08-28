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
package com.ericsson.component.aia.services.exteps.ioadapter.file;

import java.io.File;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.itpf.common.event.handler.*;

/**
 * This class implements EPS {@link EventInputHandler} and sends a collection of files downstream based on the results of
 * {@link FileDestinationResolver}
 *
 * @since 0.0.1-SNAPSHOT
 */
public class FileToLocalOutputRouter extends AbstractEventHandler implements EventInputHandler {
    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    private EventSubscriber[] subscribers;

    @Override
    protected void doInit() {
        subscribers = getRelatedSubscribers(getEventHandlerContext().getEventSubscribers());
    }

    @Override
    public void onEvent(final Object inputEvent) {
        if (inputEvent instanceof HashMap) {
            processEvent(inputEvent);
        } else {
            LOG.error("Event Not Recognized!! {}", inputEvent);
        }
    }

    /**
     * @param inputEvent
     */
    private void processEvent(final Object inputEvent) {
        @SuppressWarnings("unchecked")
        final HashMap<Key, ArrayList<File>> fileMap = (HashMap<Key, ArrayList<File>>) inputEvent;

        for (final Key nodeNameROPTimeKey : fileMap.keySet()) {
            final EventSubscriber eventSubscriber = FileDestinationResolver.resolveDestination(nodeNameROPTimeKey, subscribers);
            sendEventToSubscriber(fileMap.get(nodeNameROPTimeKey), eventSubscriber);
        }
    }

    private void sendEventToSubscriber(final Object inputEvent, final EventSubscriber eventSubscriber) {
        LOG.debug("{} sent to {}", inputEvent, eventSubscriber.getIdentifier());
        eventSubscriber.sendEvent(inputEvent);
    }

    private EventSubscriber[] getRelatedSubscribers(final Collection<EventSubscriber> subscribers) {
        final List<EventSubscriber> subscriberList = new ArrayList<EventSubscriber>();

        for (final EventSubscriber subscriber : subscribers) {
            subscriberList.add(subscriber);
        }

        LOG.debug("Found {} event subscribers", subscriberList.size());
        return subscriberList.toArray(new EventSubscriber[0]);
    }
}
