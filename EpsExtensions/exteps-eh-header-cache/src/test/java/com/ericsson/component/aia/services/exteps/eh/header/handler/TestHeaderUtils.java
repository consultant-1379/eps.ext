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

package com.ericsson.component.aia.services.exteps.eh.header.handler;

import com.ericsson.component.aia.mediation.parsers.streamrecord.StreamedRecord;

public class TestHeaderUtils {

    public static final int SOURCE_ID = 1;

    public static StreamedRecord getConnectionEvent(final int sourceId) {
        final StreamedRecord connect = new StreamedRecord(sourceId);
        connect.setAction(StreamedRecord.Actions.CONNECT);
        connect.setData(new byte[] {});
        connect.setRemoteIP(new byte[] {});
        return connect;

    }

    public static StreamedRecord getPayloadEvent(final int sourceId) {
        final StreamedRecord event = new StreamedRecord(sourceId);
        event.setAction(StreamedRecord.Actions.EVENT);
        event.setData(new byte[] {});
        event.setRemoteIP(new byte[] {});
        return event;
    }
}
