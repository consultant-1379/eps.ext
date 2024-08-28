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

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

public class SolrOutputParameterTest {

    private static SolrOutputParameter solrOutputParameter;

    @Before
    public void setup() {

        solrOutputParameter = new SolrOutputParameter();
    }

    @Test(expected = IllegalArgumentException.class)
    public void init_ZKQuorumIsNull_IllegalArgumentExceptionThrown() {
        final Properties properties = SolrTestUtil.fillProperties("", "collection1", "true");
        SolrTestUtil.verifyParameters(properties, solrOutputParameter);
    }

    @Test
    public void init_BatchSizeCacheSize_IllegalArgumentExceptionThrown() {
        try {
            final Properties properties = SolrTestUtil.fillProperties("127.0.0.1:1266", "collection1", "true");
            properties.put(SolrTestUtil.BATCH_SIZE, "3000");
            properties.put(SolrTestUtil.CACHE_SIZE, "2000");
            SolrTestUtil.verifyParameters(properties, solrOutputParameter);
            fail("Should not get here, Exception should be thrown");
        } catch (final Exception e) {
            assertTrue(e.getClass().toString().contains("IllegalArgumentException"));
            assertEquals("Cache size should be greater than or equal batch size.", e.getMessage());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void init_serverCommitWithRollBack_IllegalArgumentExceptionThrown() {
        final Properties properties = SolrTestUtil.fillProperties("127.0.0.1:1266", "collection1", "false");
        SolrTestUtil.verifyParameters(properties, solrOutputParameter);
    }

    @Test(expected = IllegalArgumentException.class)
    public void init_collectionBothNull_IllegalArgumentExceptionThrown() {
        final Properties properties = SolrTestUtil.fillProperties("127.0.0.1:1266", "", "true");
        SolrTestUtil.verifyParameters(properties, solrOutputParameter);
    }

    @Test(expected = IllegalArgumentException.class)
    public void init_illegalCollectionClass_IllegalArgumentExceptionThrown() {
        final Properties properties = SolrTestUtil.fillProperties("127.0.0.1:1266", "collection1", "true");
        properties.put(SolrTestUtil.SOLR_COLLECTION_BUILDER, "com.ericsson.component.aia.services.exteps.ioadapter.solr.TestSolrOutputAdapter");
        SolrTestUtil.verifyParameters(properties, solrOutputParameter);
    }

    @Test(expected = IllegalArgumentException.class)
    public void init_illegalResubmitTime_IllegalArgumentExceptionThrown() {
        final Properties properties = SolrTestUtil.fillProperties("127.0.0.1:1266", "collection1", "true");
        properties.put(SolrTestUtil.SOLR_RESUBMIT_TIME, "-1");
        properties.put(SolrTestUtil.SOLR_COLLECTION_BUILDER, "com.ericsson.component.aia.services.exteps.ioadapter.solr.TestSolrCollection");
        SolrTestUtil.verifyParameters(properties, solrOutputParameter);
    }

    @Test(expected = RuntimeException.class)
    public void init_normalCase_RuntimeExceptionThrown() {
        final Properties properties = SolrTestUtil.fillProperties("127.0.0.1:1266", "", "true");
        properties.put(SolrTestUtil.SOLR_RESUBMIT_TIME, "1");
        properties.put(SolrTestUtil.SOLR_COLLECTION_BUILDER, "com.ericsson.component.aia.services.exteps.ioadapter.solr.SolrCollectionTest");
        SolrTestUtil.verifyParameters(properties, solrOutputParameter);
    }

    @Test(expected = IllegalArgumentException.class)
    public void init_illegalBatchSize_IllegalArgumentExceptionThrown() {
        final Properties properties = SolrTestUtil.fillProperties("127.0.0.1:1266", "collection1", "true");
        properties.put(SolrTestUtil.SOLR_RESUBMIT_TIME, "1");
        properties.put(SolrTestUtil.BATCH_SIZE, "-1");
        properties.put(SolrTestUtil.SOLR_COLLECTION_BUILDER, "com.ericsson.component.aia.services.exteps.ioadapter.solr.TestSolrCollection");
        SolrTestUtil.verifyParameters(properties, solrOutputParameter);
    }

    @Test(expected = IllegalArgumentException.class)
    public void init_illegalCacheSize_IllegalArgumentExceptionThrown() {
        final Properties properties = SolrTestUtil.fillProperties("127.0.0.1:1266", "collection1", "true");
        properties.put(SolrTestUtil.SOLR_RESUBMIT_TIME, "1");
        properties.put(SolrTestUtil.CACHE_SIZE, "-1");
        properties.put(SolrTestUtil.SOLR_COLLECTION_BUILDER, "com.ericsson.component.aia.services.exteps.ioadapter.solr.TestSolrCollection");
        SolrTestUtil.verifyParameters(properties, solrOutputParameter);
    }

    @Test(expected = IllegalArgumentException.class)
    public void init_illegalOverwrite_IllegalArgumentExceptionThrown() {
        final Properties properties = SolrTestUtil.fillProperties("127.0.0.1:1266", "collection1", "true");
        properties.put(SolrTestUtil.SOLR_RESUBMIT_TIME, "1");
        properties.put(SolrTestUtil.SOLR_COLLECTION_BUILDER, "com.ericsson.component.aia.services.exteps.ioadapter.solr.TestSolrCollection");
        properties.put(SolrTestUtil.SOLR_OVERWRITE, "false1");
        SolrTestUtil.verifyParameters(properties, solrOutputParameter);
    }
}
