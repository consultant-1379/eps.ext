/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.component.aia.services.exteps.io.adapter.streaming.constants;

/**
 * Class to hold the type of Streaming data
 */
public class StreamType {

    private final String streamType;
    private final String suffixString;

    /**
     * Constructor, instantiate streamType
     * @param streamType
     *        StremType value
     */
    public StreamType (final String streamType) {
        this.streamType = streamType;
        this.suffixString = streamType != null ? "_" + streamType.trim() : "";
    }

    /**
     * returning a modified value by adding streamType as a suffix
     *
     * @return String
     */
    public String getSuffixString() {
        return suffixString;
    }

    /**
     *
     * @return String
     */
    public String toString() {
        return getClass().getName() + "@" + streamType;
    }
}
