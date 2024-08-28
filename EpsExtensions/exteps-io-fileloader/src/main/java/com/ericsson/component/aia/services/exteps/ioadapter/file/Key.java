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

import org.apache.commons.lang.StringUtils;

/**
 * This class holds information on the file that has been collected, and is used by {@link FileDestinationHandler} to determine which EPS subscriber
 * to send the collection of files to.
 *
 * @since 0.0.1-SNAPSHOT
 */
public class Key {

    String nodeName;

    Long ropTime;

    public Key(final String nodeName, final Long ropTime) {
        this.nodeName = nodeName;
        this.ropTime = ropTime;
    }

    public String getNodeName() {
        return nodeName;
    }

    public Long getRopTime() {
        return ropTime;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (nodeName == null ? 0 : nodeName.hashCode());
        result = prime * result + (ropTime == null ? 0 : ropTime.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Key) {
            final Key other = (Key) obj;
            return StringUtils.equals(nodeName, other.nodeName) && ropTime != null && ropTime.equals(other.ropTime);
        }
        return false;
    }
}
