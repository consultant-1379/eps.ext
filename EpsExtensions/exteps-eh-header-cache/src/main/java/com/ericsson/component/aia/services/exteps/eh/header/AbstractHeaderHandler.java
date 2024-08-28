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

package com.ericsson.component.aia.services.exteps.eh.header;

import java.util.HashSet;
import java.util.Set;

import com.ericsson.component.aia.itpf.common.event.handler.AbstractApplicationEventHandler;
import com.ericsson.component.aia.mediation.parsers.streamrecord.StreamedRecord;

public abstract class AbstractHeaderHandler extends AbstractApplicationEventHandler {

    protected final Set<Integer> localCache = new HashSet<>();

    /**
     * Invoked by flow engine on receipt of either [CONNECT,EVENT,DISCONNECT] messages.
     *
     * @param object
     *            is the object containing either [CONNECT,EVENT,DISCONNECT] message headers.
     */
    @Override
    public void inputEvents(final Object object) {
        if (object instanceof StreamedRecord) {
            final StreamedRecord record = (StreamedRecord) object;
            final StreamedRecord.Actions action = record.getAction();
            switch (action) {
                case CONNECT:
                    handleConnect(record);
                    break;
                case EVENT:
                    handlePayload(record);
                    break;
                case DISCONNECT:
                    handleDisconnect(record);
                    break;
                default:
                    sendEvent(record);
            }
        }
    }

    private void handleConnect(final StreamedRecord connectRecord) {
        final int sourceId = connectRecord.getSourceId();

        if (localCache.contains(sourceId)) {
            log.debug("Received CONNECT message again from the same sourceId={}", sourceId);
        } else {
            sendEvent(connectRecord);
            localCache.add(sourceId);
            log.debug("Received CONNECT message for sourceId={}", sourceId);
        }

        handleConnectionEvent(connectRecord);
    }

    private void handleDisconnect(final StreamedRecord disconnectRecord) {
        final int sourceId = disconnectRecord.getSourceId();
        log.debug("Received DISCONNECT message from sourceId={}", sourceId);

        if (!localCache.remove(sourceId)) {
            log.debug("CONNECT header doesn't exist in local cache for: sourceId={}", sourceId);
        }
        sendEvent(disconnectRecord);

        handleDisconnectEvent(disconnectRecord);
    }

    private void handlePayload(final StreamedRecord eventRecord) {
        final int sourceId = eventRecord.getSourceId();

        if (!localCache.contains(sourceId)) {
            //get header from remote source and resend.
            resendHeader(eventRecord);
        }
        sendEvent(eventRecord);
    }

    protected abstract void handleConnectionEvent(final StreamedRecord record);

    protected abstract void handleDisconnectEvent(final StreamedRecord record);

    protected abstract void resendHeader(final StreamedRecord record);
}
