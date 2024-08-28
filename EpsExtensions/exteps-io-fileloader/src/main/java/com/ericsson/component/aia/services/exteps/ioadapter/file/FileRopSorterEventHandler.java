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
import java.text.ParseException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler;
import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;

/**
 * This class implements EPS {@link EventInputHandler}.
 * It receives a collection of files and instantiates {@link FileRopSorter} which returns a map of files to be sent downstream.
 *
 * @since 0.0.1-SNAPSHOT
 */
public class FileRopSorterEventHandler extends AbstractEventHandler implements EventInputHandler {

    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    private FileRopSorter fileProcessor;

    @Override
    protected void doInit() {
        fileProcessor = new FileRopSorter();
    }

    protected void sendEvent(final Object event) {
        sendToAllSubscribers(event);
        LOG.debug("Sent {} to subscribers", event);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onEvent(final Object inputEvent) {
        if (inputEvent instanceof List<?>) {
            processEvent((List<File>) inputEvent);
        } else {
            LOG.error("Event Not Recognized!! {}", inputEvent);
        }
    }

    private void processEvent(final List<File> inputEvent) {
        try {
            final Map<Key, ArrayList<File>> fileRopMap = fileProcessor.sendFilesByROP(inputEvent);
            sendEvent(fileRopMap);
        } catch (final ParseException e) {
            LOG.debug("ParseException caught: ", e.getMessage());
        }
    }

}
