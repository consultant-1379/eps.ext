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
package com.ericsson.component.aia.services.exteps.ioadapter.solr;

import java.util.TimeZone;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ericsson.component.aia.services.exteps.ioadapter.solr.external.collectionbuilder.RetentionCollectionBuilder;

/**
 * @author esarlag
 * 
 */
public class CollectionBuilderTest extends RetentionCollectionBuilder {

    static String timeZone;
    long time;

    @BeforeClass
    public static void setup() {

        timeZone = TimeZone.getDefault().getDisplayName();
    }

    @Test
    public void test_buildCollection() {

        final int retentionDay = 1;
        setTime(1429090979219L);
        final String result = buildCollection(retentionDay, timeZone);

        Assert.assertNotNull(result);
        Assert.assertTrue("Positive value method failure", result.endsWith("1"));
    }

    @Test
    public void test_buildCollection_negative() {

        final int retentionDay = -10;
        setTime(1429090979219L);
        final String result = buildCollection(retentionDay, timeZone);

        Assert.assertNotNull(result);
        Assert.assertTrue("Negative value method failure", result.endsWith("4"));
    }

    @Test
    public void test_deprecated() {
        final String result = buildCollection();
        setTime(1429090979219L);
        Assert.assertNotNull(result);
        Assert.assertTrue("Deprecated method failure", result.endsWith("0"));
    }

    @Override
    protected long getTime() {
        return time;
    }

    public void setTime(final long time) {
        this.time = time;
    }
}
