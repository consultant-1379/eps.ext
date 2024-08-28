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
package com.ericsson.component.aia.services.exteps.ioadapter.solr.external.collectionbuilder;

import java.util.TimeZone;

import com.ericsson.component.aia.services.exteps.ioadapter.solr.external.SolrCollectionBuilder;

public class RetentionCollectionBuilder implements SolrCollectionBuilder {

    private static final int SECONDS_IN_DAY = 86400;
    private static final int MILLIS_IN_SECOND = 1000;
    private static final int DEFAULT_RETENTION_DAY = 7;
    private static final String DEPRECATED_DEFAULT_TIME_ZONE = "JST";
    private static final int RETENTION_DAY_BUFFER = 2;

    @Override
    @Deprecated
    public String buildCollection() {
        return buildCollection(DEFAULT_RETENTION_DAY, DEPRECATED_DEFAULT_TIME_ZONE);
    }

    @Override
    public String buildCollection(final int retentionDay, final String timeZone) {
        final long time = getTime();
        final int daysFromZero = (int) ((time + TimeZone.getTimeZone(timeZone).getOffset(time)) / MILLIS_IN_SECOND / SECONDS_IN_DAY); //get the day no from 1970-01-01
        return "collection" + (daysFromZero % (retentionDay + RETENTION_DAY_BUFFER));
    }

    protected long getTime() {
        return System.currentTimeMillis();
    }

}
