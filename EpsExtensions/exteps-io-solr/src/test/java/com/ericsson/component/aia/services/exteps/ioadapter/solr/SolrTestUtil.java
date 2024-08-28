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

import java.util.Properties;

import com.ericsson.component.aia.services.exteps.ioadapter.solr.SolrOutputParameter;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;

public class SolrTestUtil {

    public static final String ZK_QUORUM_PARAM_NAME = "zkQuorum";
    public static final String ZK_CLIENT_TIMEOUT_PARAM_NAME = "zkClientTimeout";
    public static final String ZK_CONNECTION_TIMEOUT_PARAM_NAME = "zkConnectionTimeout";
    public static final String SOLR_COLLECTION = "solrCollection";
    public static final String SOLR_CLIENT_COMMIT = "solrClientCommit";
    public static final String SOLR_COMMIT_WAIT_FLUSH = "waitFlush";
    public static final String SOLR_COMMIT_WAIT_SEARCHER = "waitSearcher";
    public static final String SOLR_COMMIT_SOFT_COMMIT = "softCommit";
    public static final String SOLR_COMMIT_EXCEPTION_HANDLER = "exceptionHandler";
    public static final String SOLR_RESUBMIT_TIME = "resubmitTime";
    public static final String SOLR_COLLECTION_BUILDER = "solrCollectionBuilder";
    public static final String BATCH_SIZE = "batchSize";
    public static final String CACHE_SIZE = "cacheSize";
    public static final String SOLR_OVERWRITE = "solrOverwrite";

    public static Properties fillProperties(final String zkQuorumParam, final String solrCollection, final String solrClientCommit) {
        final Properties properties = new Properties();
        properties.put(ZK_QUORUM_PARAM_NAME, zkQuorumParam);
        properties.put(ZK_CLIENT_TIMEOUT_PARAM_NAME, "21000");
        properties.put(ZK_CONNECTION_TIMEOUT_PARAM_NAME, "31000");
        properties.put(SOLR_COLLECTION, solrCollection);
        properties.put(SOLR_CLIENT_COMMIT, solrClientCommit);
        properties.put(SOLR_COMMIT_WAIT_FLUSH, "true");
        properties.put(SOLR_COMMIT_WAIT_SEARCHER, "true");
        properties.put(SOLR_COMMIT_SOFT_COMMIT, "false");
        properties.put(SOLR_COMMIT_EXCEPTION_HANDLER, "rollback");
        properties.put(SOLR_RESUBMIT_TIME, "2");
        properties.put(SOLR_COLLECTION_BUILDER, "");
        return properties;
    }

    public static void verifyParameters(final Properties properties, final SolrOutputParameter solrOutputParameter) throws RuntimeException {
        final EventHandlerContext eventHandlerContext = new TestSolrOutputEventHandlerContext(properties);
        solrOutputParameter.parameterCollect(eventHandlerContext.getEventHandlerConfiguration());
        solrOutputParameter.parameterCheck();
    }

}
