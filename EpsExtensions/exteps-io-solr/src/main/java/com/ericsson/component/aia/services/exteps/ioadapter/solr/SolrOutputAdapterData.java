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

public class SolrOutputAdapterData {

    //2 values: resubmit and rollback
    public static final String DEFAULT_SOLR_COMMIT_EXCEPTION_HANDLER_RESUBMIT = "resubmit";
    public static final String DEFAULT_SOLR_COMMIT_EXCEPTION_HANDLER_ROLLBACK = "rollback";
    public static final String DEFAULT_SOLR_OVERWRITE = "true";
    public static final String SOLR_OVERWRITE_FALSE = "false";

    static final int DEFAULT_ZK_CONNECTION_TIMEOUT = 30000;
    static final int DEFAULT_ZK_CLIENT_TIMEOUT = 10000;
    static final int DEFAULT_SOLR_RESUBMIT_TIME = 1;
    static final boolean DEFAULT_SOLR_CLIENT_COMMIT = false;
    static final boolean DEFAULT_SOLR_COMMIT_WAIT_FLUSH = false;
    static final boolean DEFAULT_SOLR_COMMIT_WAIT_SEARCHER = false;
    static final boolean DEFAULT_SOLR_COMMIT_SOFT_COMMIT = true;
    static final int DEFAULT_CACHE_SIZE = 30000;
    static final int DEFAULT_BATCH_SIZE = 1000;
    static final int DEFAULT_HTTP_SOCKET_TIMEOUT = 60000;
    static final int DEFAULT_HTTP_CONNECTION_TIMEOUT = 60000;
    static final int DEFAULT_HTTP_MAX_CONNECTIONS = 1000;
    static final int DEFAULT_HTTP_MAX_CONNECTIONS_PER_HOST = 200;

    static final String URI = "solr:/";
    static final String ZK_QUORUM_PARAM_NAME = "zkQuorum";
    static final String ZK_CLIENT_TIMEOUT_PARAM_NAME = "zkClientTimeout";
    static final String ZK_CONNECTION_TIMEOUT_PARAM_NAME = "zkConnectionTimeout";
    static final String SOLR_COLLECTION = "solrCollection";
    static final String SOLR_CLIENT_COMMIT = "solrClientCommit";
    static final String SOLR_COMMIT_WAIT_FLUSH = "waitFlush";
    static final String SOLR_COMMIT_WAIT_SEARCHER = "waitSearcher";
    static final String SOLR_COMMIT_SOFT_COMMIT = "softCommit";
    static final String SOLR_COMMIT_EXCEPTION_HANDLER = "exceptionHandler";
    static final String SOLR_RESUBMIT_TIME = "resubmitTime";
    static final String BATCH_SIZE = "batchSize";
    static final String CACHE_SIZE = "cacheSize";
    static final String SOLR_COLLECTION_BUILDER = "solrCollectionBuilder";
    static final String SOLR_OVERWRITE = "solrOverwrite";
    static final String SYS_SOLR_ALIAS = "sys.SolrAlias";
    static final String EPS_EXT = "epsExt";
    static final String SOLR_RECORDS_RECEIVED = "solrRecordsReceived";
    static final String SOLR_RECORDS_INDEXED = "solrRecordsIndexed";
    static final String HTTP_CONNECTION_TIMEOUT = "httpConnectionTimeout";
    static final String HTTP_SOCKET_TIMEOUT = "httpSocketTimeout";
    static final String HTTP_MAX_CONNECTIONS = "httpMaxConnections";
    static final String HTTP_MAX_CONNECTIONS_PER_HOST = "httpMaxConnectionsPerHost";
    static final String RETENTION_DAY = "retentionDay";
    static final String TIME_ZONE = "timeZone";

    final String SOLR_OUTPUT_ADAPTER = SolrOutputAdapter.class.getSimpleName();
}