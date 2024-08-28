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
package com.ericsson.component.aia.services.exteps.io.adapter.streaming.utils.torserver;

public class StreamOutProtocol {

    public static byte[] getInitilizationMessage() {
        final byte[] initializationMessage = new byte[16];

        //byte 1 : message type, 0x01 for initialization message
        initializationMessage[0] = 0x01;

        //byte 2-4 : source id
        initializationMessage[1] = 0x00;
        initializationMessage[2] = 0x00;
        initializationMessage[3] = 0x01;

        //byte 5-6 : length
        initializationMessage[4] = 0x00;
        initializationMessage[5] = 0x0c; //12 bytes (2 bytes for length + 8 bytes for protocol + 2 bytes for reserved for future use)

        //byte 7-14 protocol version
        padWithZero(initializationMessage, 6, 13);

        //byte 15-16 reserved for future use
        initializationMessage[14] = 0x00;
        initializationMessage[15] = 0x01;
        return initializationMessage;
    }

    public static byte[] getConnectionMessage() {
        final byte[] connectionMessage = new byte[39];

        //byte 1 : message type, 0x02 for connection message
        connectionMessage[0] = 0x02;

        //byte 2-4 : source id
        padWithZero(connectionMessage, 1, 2);
        connectionMessage[3] = 0x01;

        //byte 5-6 : length (2 + 8 + 16 + 9 = 35bytes long....35 in decimal is 0x23 in hex)
        connectionMessage[4] = 0x00;
        connectionMessage[5] = 0x23;

        //byte 7-14 timestamp
        connectionMessage[6] = 0x01;
        connectionMessage[7] = 0x02;
        connectionMessage[8] = 0x03;
        connectionMessage[9] = 0x04;
        connectionMessage[10] = 0x05;
        connectionMessage[11] = 0x05;
        connectionMessage[12] = 0x07;
        connectionMessage[13] = 0x08;

        //byte 7-14 ip address (10.45.90.1)
        padWithZero(connectionMessage, 14, 25);
        connectionMessage[26] = 0x0A;
        connectionMessage[27] = 0x2D;
        connectionMessage[28] = 0x5A;
        connectionMessage[29] = 0x01;

        //variable length = var length from initialization message
        connectionMessage[30] = 0x05;
        connectionMessage[31] = 0x00;
        //connectionMessage[31] = 0x01;

        //header event

        connectionMessage[32] = 0x08;
        connectionMessage[33] = 0x00;
        connectionMessage[34] = 0x01;
        connectionMessage[35] = 0x02;
        connectionMessage[36] = 0x03;
        connectionMessage[37] = 0x04;
        connectionMessage[38] = 0x05;
        return connectionMessage;
    }

    public static byte[] getEventMessage() {
        // ************************
        // Event Message
        // ************************

        final byte[] eventMessage = new byte[14];

        //byte 1 : message type, 0x00 for event message
        eventMessage[0] = 0x00;

        //byte 2-4 : source id
        padWithZero(eventMessage, 1, 2);
        eventMessage[3] = 0x01;

        // length of message excluding header but including these 2 bytes (0x0A = 10 bytes for eventMessage[4] - eventMessage[13]
        eventMessage[4] = 0x00;
        eventMessage[5] = 0x0A;

        // event payload
        eventMessage[6] = 0x01;
        eventMessage[7] = 0x02;
        eventMessage[8] = 0x03;
        eventMessage[9] = 0x04;
        eventMessage[10] = 0x05;
        eventMessage[11] = 0x06;
        eventMessage[12] = 0x07;
        eventMessage[13] = 0x08;
        return eventMessage;
    }

    public static byte[] getDisconnectMessage() {
        // ************************
        // Disconnection Message
        // ************************

        final byte[] disconnectionMessage = new byte[16];

        // message type
        disconnectionMessage[0] = 0x03;

        // source_id
        padWithZero(disconnectionMessage, 1, 2);
        disconnectionMessage[3] = 0x01;

        // length (excluding header but including this length part)
        disconnectionMessage[4] = 0x00;
        disconnectionMessage[5] = 0x0C;

        // timestamp
        padWithZero(disconnectionMessage, 6, 7);
        disconnectionMessage[8] = 0x01;
        disconnectionMessage[9] = 0x3C;
        disconnectionMessage[10] = 0x0A;
        disconnectionMessage[11] = 0x1C;
        disconnectionMessage[12] = 0x32;
        disconnectionMessage[13] = 0x08;

        // Reason
        disconnectionMessage[14] = 0x00;
        disconnectionMessage[15] = 0x03;
        return disconnectionMessage;
    }

    public static byte[] getDroppedEventMessage() {

        final byte[] message = new byte[24];

        //byte 1 : message type, 0x00 for event message
        message[0] = 0x04;

        //byte 2-4 : source id
        padWithZero(message, 1, 4);
        message[5] = 0x14;

        //byte 7-14 timestamp
        message[6] = 0x01;
        message[7] = 0x02;
        message[8] = 0x03;
        message[9] = 0x04;
        message[10] = 0x05;
        message[11] = 0x05;
        message[12] = 0x07;
        message[13] = 0x08;

        //Reason
        message[14] = 0x00;
        message[15] = 0x01;

        //Dropped event count
        padWithZero(message, 16, 22);
        message[23] = 0x01;
        return message;
    }

    public static void padWithZero(final byte[] message, final int startIndex, final int endIndex) {
        for (int i = startIndex; i <= endIndex; i++) {
            message[i] = 0x00;
        }
    }
}
