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
package com.ericsson.component.aia.services.exteps.io.adapter.util;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.itpf.common.event.handler.AbstractApplicationEventHandler;

/**
 * The Class PrintMessage. Simple logging handler.
 */
public class PrintMessage extends AbstractApplicationEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrintMessage.class);

    @Override
    protected void doInit() {
    }

    @Override
    public boolean understandsURI(final String uri) {
        return true;
    }

    @Override
    protected void inputEvents(Object object) {
        if (object instanceof byte[]) {
            object = Arrays.toString((byte[]) object);
        }
        LOGGER.warn(String.valueOf(object));
        sendEvent(object);
    }

    @Override
    public void destroyAll() {
    }

}
