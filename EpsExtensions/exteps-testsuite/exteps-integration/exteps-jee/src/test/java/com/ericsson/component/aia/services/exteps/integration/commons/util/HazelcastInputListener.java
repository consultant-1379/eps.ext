/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.exteps.integration.commons.util;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

public class HazelcastInputListener extends MessagingTestListener implements MessageListener<Object> {

    public HazelcastInputListener(final int count) {
        super.initCountDown(count);
    }

    @Override
    public void onMessage(final Message<Object> msg) {
        this.mark(msg);
    }

}
