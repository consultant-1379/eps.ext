/*
 * ------------------------------------------------------------------------------
 * *****************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 * *****************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.component.aia.services.exteps.eh.header.cache;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class will simulate the active parser node processing the CONNECT message. Under the hood, the distributed cache
 * should take care of distributing this message to the other node - {@link EventHeaderCacheHandler2Test}.
 */
@Ignore("Disabled when running on Jenking because of the timming coordination (sleep)")
public class EventHeaderCacheHandler1Test extends EventHeaderCacheHandlerTestBase {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Test
    public void processing_connect_message_should_distribute_it_to_other_nodes() {
        log.info("Waiting 40 seconds for other node...");
        sleepSeconds(40);
        log.info("Sending CONNECT message...");
        getHandler().inputEvents(newConnect(SOURCE_ID));
    }
}