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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.itpf.common.event.handler.EventSubscriber;

/**
 * This class uses the {@link Key} class to get the node name of a collection of files and
 * determine which subscriber to send them to using a modular on the hash code of the key.
 *
 * @since 0.0.1-SNAPSHOT
 */
public class FileDestinationResolver {

    protected static final Logger LOG = LoggerFactory.getLogger(FileDestinationResolver.class);

    public static EventSubscriber resolveDestination(final Key nodeNameROPTimeKey, final EventSubscriber[] subscribers) {
        int subscriberAllocation = nodeNameROPTimeKey.getNodeName().hashCode() % subscribers.length;

        if (subscriberAllocation < 0) {
            subscriberAllocation *= -1;
        }

        LOG.debug("Node {} sent to {}", nodeNameROPTimeKey.getNodeName(), subscribers[subscriberAllocation]);
        return subscribers[subscriberAllocation];
    }
}
