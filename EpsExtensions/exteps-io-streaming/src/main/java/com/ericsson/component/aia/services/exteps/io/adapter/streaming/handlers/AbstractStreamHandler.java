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
package com.ericsson.component.aia.services.exteps.io.adapter.streaming.handlers;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.mediation.netty.protocol.parser.AbstractMuxEvent;
import com.ericsson.component.aia.mediation.parsers.streamrecord.StreamedRecord;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.listener.RecordListener;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.listener.StreamingListener;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.util.EventMetrics;

/**
 * This class is responsible for handling the stream messages.
 *
 * @since 1.0.7
 *
 **/
public abstract class AbstractStreamHandler {

    protected static final Map<Integer, byte[]> SOURCEID_IPADDRESS = new HashMap<Integer, byte[]>();
    protected static final Set<Integer> VALID_SRC_SET = new TreeSet<Integer>();
    protected static final Set<Integer> INVALID_SRC_SET = new TreeSet<Integer>();
    protected static final Logger LOGGER = LoggerFactory.getLogger(StreamingListener.class);
    protected static final long PROTOCOL_VERSION = 281479271743489L;

    private final transient RecordListener streamListener;
    private final transient EventMetrics receivedMetrics;


    public AbstractStreamHandler() {
        streamListener = StreamingListener.getController().getStreamListener();
        receivedMetrics = EventMetrics.getInstance();
    }

    /**
     * @param event
     *
     */
    public abstract void handle(AbstractMuxEvent event);

    /**
     * Offers the StreamRecord.
     *
     * @param streamedRecord
     *
     */
    public void offer(final StreamedRecord streamedRecord) {
        streamListener.streamReceived(streamedRecord);
    }

    protected void incrementEvents() {
        receivedMetrics.incrementEvents();
    }

    protected void noSourceId() {
        receivedMetrics.incrementNoSrc();
    }

    protected void incrementConnects() {
        receivedMetrics.incrementConnects();
    }

    protected void incrementDisconnects() {
        receivedMetrics.incrementDisconnects();
    }

    protected void incrementDrops() {
        receivedMetrics.incrementDrops();
    }

    protected void lostRecords(final long incrementAmount) {
        receivedMetrics.incrementLostRecords(incrementAmount);
    }

    public EventMetrics getReceivedMetrics() {
        return receivedMetrics;
    }
}
