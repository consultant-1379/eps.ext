/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.exteps.io.adapter.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;

import com.codahale.metrics.Meter;
import com.ericsson.component.aia.itpf.common.config.Configuration;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.component.aia.mediation.netty.protocol.StreamOutProtocol.EventType;
import com.ericsson.component.aia.mediation.parsers.streamrecord.StreamedRecord;
import com.ericsson.component.aia.services.eps.coordination.EpsAdaptiveConfiguration;
import com.ericsson.component.aia.services.exteps.io.adapter.ipl.StubbedContext;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class RawBytesConverterTest {

    private static final int LENGTH_FIELD_SIZE = 2;
    private static final byte[] SOURCE_ID = new byte[] { 0, 0, 1 };
    private static final byte[] EVENT_PAYLOAD = new byte[] { 1, 1, 1, 1, 1, 1, 1, 1 };
    private static final int PAYLOAD_EVENT_LENGTH = EVENT_PAYLOAD.length + LENGTH_FIELD_SIZE;
    private static final byte[] TIMESTAMP = new byte[] { 5, 5, 5, 5, 5, 5, 5, 5 };
    private static final byte[] PROTOCOL_VERSION = new byte[] { 2, 2, 2, 2, 2, 2, 2, 2 };
    private static final byte[] DROPPED_EVENTS = new byte[] { 9, 9, 9, 9, 9, 9, 9, 9 };
    private static final byte[] IPADDRESS = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };
    private static final int CONNECTION_EVENT_LENGTH = EVENT_PAYLOAD.length + LENGTH_FIELD_SIZE + TIMESTAMP.length + IPADDRESS.length;
    private static final int REASON = 1;
    private static final int VAR_LENGTH = 2;
    private static final int REASON_LENGTH = 2;
    private static final int DISCONNECTION_EVENT_LENGTH = LENGTH_FIELD_SIZE + TIMESTAMP.length + REASON_LENGTH;
    private static final int INIT_EVENT_LENGTH = DISCONNECTION_EVENT_LENGTH;
    private static final int DROPPED_EVENT_LENGTH = DISCONNECTION_EVENT_LENGTH + DROPPED_EVENTS.length;

    private final RawBytesConverter handler = new RawBytesConverter();

    @Before
    public void setUp() {
        EventHandlerContext stubbedEventHandlerContext = new StubbedContext(new EpsAdaptiveConfiguration());
        Whitebox.setInternalState(handler, "eventHandlerContext", stubbedEventHandlerContext);
        handler.doInit();
    }

    @Test
    public void testConnectionEventIsParsedCorrectly() {
        final List<StreamedRecord> records = handler.splitBytesIntoStreamedRecords(connectionEvent());
        assertEquals(1, records.size());
        final StreamedRecord streamedRecord = records.get(0);
        assertCorrectConnectionRecord(streamedRecord);
    }

    @Test
    public void testPayloadEventIsParsedCorrectly() {
        final List<StreamedRecord> records = handler.splitBytesIntoStreamedRecords(payloadEvent());
        assertEquals(1, records.size());
        final StreamedRecord streamedRecord = records.get(0);
        assertCorrectPayloadRecord(streamedRecord);
    }

    @Test
    public void testDisconnectionEventIsParsedCorrectly() {
        final List<StreamedRecord> records = handler.splitBytesIntoStreamedRecords(disconnectionEvent());
        assertEquals(1, records.size());
        final StreamedRecord streamedRecord = records.get(0);
        assertCorrectDisconnectionRecord(streamedRecord);
    }

    @Test
    public void testInitEventIsNotParsed() {
        final List<StreamedRecord> records = handler.splitBytesIntoStreamedRecords(initEvent());
        assertTrue(records.isEmpty());
    }

    @Test
    public void testDroppedEventIsNotParsed() {
        final List<StreamedRecord> records = handler.splitBytesIntoStreamedRecords(droppedEvent());
        assertTrue(records.isEmpty());
    }

    @Ignore("There's a bug in StreamOutProtocolParser that will throw null pointers if an incorrect eventType byte is used. Needs to be fixed before this test can run")
    @Test
    public void testUnknownEventIsNotParsed() {
        final List<StreamedRecord> records = handler.splitBytesIntoStreamedRecords(unknownEvent());
        assertTrue(records.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullEventIsNotParsed() {
        handler.splitBytesIntoStreamedRecords(null);
    }

    @Test
    public void testMultipleEventsAreSplitCorrectly() {
        final ByteBuf buf = Unpooled.buffer();
        buf.writeBytes(connectionEvent());
        buf.writeBytes(payloadEvent());
        buf.writeBytes(disconnectionEvent());
        buf.writeBytes(initEvent());
        buf.writeBytes(droppedEvent());
        buf.writeBytes(droppedEvent());
        final byte[] overAllEvents = handler.toByteArray(buf);
        handler.checkInputEventType(overAllEvents);
        final List<StreamedRecord> records = handler.splitBytesIntoStreamedRecords(overAllEvents);
        assertEquals(3, records.size());
        assertCorrectConnectionRecord(records.get(0));
        assertCorrectPayloadRecord(records.get(1));
        assertCorrectDisconnectionRecord(records.get(2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void onEvent_null() {
        handler.onEvent(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void onEvent_IncorrectEventType() {
        handler.onEvent("");
    }

    private byte[] connectionEvent() {
        final ByteBuf buf = Unpooled.buffer();
        buf.writeByte(EventType.CONNECTION.toByte());
        buf.writeBytes(SOURCE_ID);
        buf.writeShort(CONNECTION_EVENT_LENGTH);
        buf.writeBytes(TIMESTAMP);
        buf.writeBytes(IPADDRESS);
        buf.writeBytes(EVENT_PAYLOAD);
        return handler.toByteArray(buf);
    }

    private byte[] payloadEvent() {
        final ByteBuf buf = Unpooled.buffer();
        buf.writeByte(EventType.PAYLOAD.toByte());
        buf.writeBytes(SOURCE_ID);
        buf.writeShort(PAYLOAD_EVENT_LENGTH);
        buf.writeBytes(EVENT_PAYLOAD);
        return handler.toByteArray(buf);
    }

    private byte[] disconnectionEvent() {
        final ByteBuf buf = Unpooled.buffer();
        buf.writeByte(EventType.DISCONNECTION.toByte());
        buf.writeBytes(SOURCE_ID);
        buf.writeShort(DISCONNECTION_EVENT_LENGTH);
        buf.writeBytes(TIMESTAMP);
        buf.writeShort(REASON);
        return handler.toByteArray(buf);
    }

    private byte[] initEvent() {
        final ByteBuf buf = Unpooled.buffer();
        buf.writeByte(EventType.INIT.toByte());
        buf.writeBytes(SOURCE_ID);
        buf.writeShort(INIT_EVENT_LENGTH);
        buf.writeBytes(PROTOCOL_VERSION);
        buf.writeShort(VAR_LENGTH);
        return handler.toByteArray(buf);
    }

    private byte[] droppedEvent() {
        final ByteBuf buf = Unpooled.buffer();
        buf.writeByte(EventType.DROPPED.toByte());
        buf.writeBytes(SOURCE_ID);
        buf.writeShort(DROPPED_EVENT_LENGTH);
        buf.writeBytes(TIMESTAMP);
        buf.writeShort(REASON);
        buf.writeBytes(DROPPED_EVENTS);
        return handler.toByteArray(buf);
    }

    private byte[] unknownEvent() {
        final ByteBuf buf = Unpooled.buffer();
        buf.writeByte(9);
        buf.writeBytes(SOURCE_ID);
        buf.writeShort(DROPPED_EVENT_LENGTH);
        buf.writeBytes(TIMESTAMP);
        buf.writeShort(REASON);
        buf.writeBytes(DROPPED_EVENTS);
        return handler.toByteArray(buf);
    }

    private byte[] getRemainingBytes(final ByteBuf dataBuffer) {
        final int remainingBytes = dataBuffer.readableBytes();
        final ByteBuf remainingBytesBuffer = dataBuffer.readBytes(remainingBytes);
        return handler.toByteArray(remainingBytesBuffer);
    }

    private ByteBuf getDataBuffer(final StreamedRecord record) {
        final byte[] data = record.getData();
        final ByteBuf dataBuffer = Unpooled.buffer(data.length).writeBytes(data);
        return dataBuffer;
    }

    private void assertCorrectConnectionRecord(final StreamedRecord streamedRecord) {
        assertEquals(StreamedRecord.Actions.CONNECT, streamedRecord.getAction());
        assertArrayEquals(IPADDRESS, streamedRecord.getRemoteIP());
        final ByteBuf dataBuffer = getDataBuffer(streamedRecord);
        assertArrayEquals(EVENT_PAYLOAD, getRemainingBytes(dataBuffer));
    }

    private void assertCorrectPayloadRecord(final StreamedRecord streamedRecord) {
        assertEquals(StreamedRecord.Actions.EVENT, streamedRecord.getAction());
        final ByteBuf dataBuffer = getDataBuffer(streamedRecord);
        assertEquals(PAYLOAD_EVENT_LENGTH, dataBuffer.readShort());
        assertArrayEquals(EVENT_PAYLOAD, getRemainingBytes(dataBuffer));
    }

    private void assertCorrectDisconnectionRecord(final StreamedRecord streamedRecord) {
        assertEquals(StreamedRecord.Actions.DISCONNECT, streamedRecord.getAction());
        assertEquals(REASON, streamedRecord.getDisconnectReason());
    }
}
