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
package com.ericsson.component.aia.services.exteps.ioadapter.solr.service;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.config.SocketConfig;
import org.apache.http.config.SocketConfig.Builder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.client.solrj.impl.BinaryRequestWriter;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;
import org.apache.solr.client.solrj.request.RequestWriter;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.cloud.ZooKeeperException;
import org.apache.solr.common.params.UpdateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.exteps.ioadapter.solr.SolrOutputAdapterData;
import com.ericsson.component.aia.services.exteps.ioadapter.solr.SolrOutputParameter;
import com.ericsson.component.aia.services.exteps.ioadapter.solr.external.SolrCollectionBuilder;

public final class SolrIndexService {

    private static SolrIndexService indexService;

    private static SolrCacheService<?> cacheService;
    private static CloudSolrServer cloudSolrServer;
    private static AtomicLong totalCount = new AtomicLong();
    private static final AtomicInteger logCount = new AtomicInteger();

    private static final int LOG_COUNT = 10000;
    private static final int MAX_EXECUTION_COUNT = 2;
    private static final String OVER_WRITE_TRUE = "true";
    private static final int NOT_FOUND = 404;
    private static final int SERVER_ERROR = 500;
    private static final int SERVICE_UNAVAILABLE = 503;


    private final SolrOutputParameter solrPara;
    private final Logger logger = LoggerFactory.getLogger(SolrIndexService.class);

    private SolrIndexService(final SolrOutputParameter solrParameter) {
        solrPara = solrParameter;
        final HttpClient httpClient = createHttpClient();
        initCloudSolrServer(httpClient);

        if (solrPara.getSolrCollection() != null && !solrPara.getSolrCollection().isEmpty()) {
            cloudSolrServer.setDefaultCollection(solrPara.getSolrCollection());
        }
        try {
            cloudSolrServer.connect();
        } catch (final ZooKeeperException e) {
            logger.error("The URL of zkHost is not correct or error connecting to Zookeeper", e);
            throw new IllegalStateException("Was not able to get connection to SolrCloud", e);
        }
    }

    public static synchronized SolrIndexService getInstance(final SolrOutputParameter solrPara) {
        if (indexService == null) {
            indexService = new SolrIndexService(solrPara);
        }
        if (cacheService == null && solrPara.getExceptionHandler() != null
                && solrPara.getExceptionHandler().equals(SolrOutputAdapterData.DEFAULT_SOLR_COMMIT_EXCEPTION_HANDLER_RESUBMIT)) {
            cacheService = new SolrCacheService<Object>(indexService, solrPara.getBatchSize(), solrPara.getCacheSize());
            cacheService.process();

        }
        return indexService;
    }

    /**
     *
     * Each document should contain document ID so no need to care about duplication issue for resubmit same document
     *
     **/
    private <T> boolean addDocs(final Collection<T> docValues, final int retryTime) throws SolrServerException, IOException {
        boolean submitOk = false;
        final SolrCollectionBuilder builder = solrPara.getSolrCollectionBuilder();
        if (builder != null) {
            if (solrPara.getTimeZone() == null) {
                cloudSolrServer.setDefaultCollection(builder.buildCollection());
            } else {
                cloudSolrServer.setDefaultCollection(builder.buildCollection(solrPara.getRetentionDay().intValue(), solrPara.getTimeZone()));
            }
        }
        try {
            addBeanByRequest(docValues, retryTime);
            commit();
            submitOk = true;
        } catch (final SolrException solrEx) {
            final boolean wasCommError = isCommonException(solrEx);
            //From Solr Client perspective, we can just ignore other exceptions
            boolean wasServerError = false;

            final int code = solrEx.code();
            wasServerError = (code == NOT_FOUND || code == SERVER_ERROR || code == SERVICE_UNAVAILABLE);

            if (wasCommError || wasServerError) {
                logger.error("Error happens when adding documents", solrEx);
                submitOk = handleException(docValues, retryTime);
            } else {
                logger.debug("This is an accepted exception and it is expected that commit is successful.", solrEx);
                submitOk = true;
            }
        } catch (final Exception ex) {
            logger.debug("This is an accepted exception and it is expected that commit is successful.", ex);
            submitOk = true;
        }
        if (submitOk) {
            updateDocsCount(docValues);
        }
        return submitOk;
    }

    private <T> void updateDocsCount(final Collection<T> docValues) {
        totalCount.addAndGet(docValues.size());
        logCount.addAndGet(docValues.size());
        if (logger.isDebugEnabled()) {
            final int num = logCount.get();
            if (num != 0 && num >= LOG_COUNT) {
                logger.debug("Have sent {} CSL events to SolrCloud.", totalCount.get());
                logCount.set(0);
            }
        }
    }

    public <T> boolean addDocs(final Collection<T> docValues) throws SolrServerException, IOException {
        return addDocs(docValues, solrPara.getResubmitTime());
    }

    private void commit() throws SolrServerException, IOException {
        if (solrPara.isSolrClientCommit()) {
            cloudSolrServer.commit(solrPara.isWaitFlush(), solrPara.isWaitSearcher(), solrPara.isSoftCommit());
        }
    }

    private <T> boolean handleException(final Collection<T> docs, final int retryTime) throws SolrServerException, IOException {
        boolean submitOk = false;
        if (solrPara.isSolrClientCommit()) {
            if (solrPara.getExceptionHandler() != null
                    && solrPara.getExceptionHandler().equalsIgnoreCase(SolrOutputAdapterData.DEFAULT_SOLR_COMMIT_EXCEPTION_HANDLER_ROLLBACK)) {
                this.rollback(docs);
            } else if (solrPara.getExceptionHandler() != null
                    && solrPara.getExceptionHandler().equals(SolrOutputAdapterData.DEFAULT_SOLR_COMMIT_EXCEPTION_HANDLER_RESUBMIT)) {
                submitOk = this.resubmit(docs, retryTime);
            }
        } else {
            if (solrPara.getExceptionHandler() != null
                    && solrPara.getExceptionHandler().equals(SolrOutputAdapterData.DEFAULT_SOLR_COMMIT_EXCEPTION_HANDLER_RESUBMIT)) {
                submitOk = this.resubmit(docs, retryTime);
            }
        }
        return submitOk;
    }

    /**
     * Performs a rollback of all non-committed documents pending.
     * <p>
     * Note that this is not a true rollback as in databases. Content you have previously added may have been committed due to autoCommit, buffer
     * full, other client performing a commit etc.
     *
     * when rollback occurs, it is impossible to calculate the missing events number
     */
    private <T> void rollback(final Collection<T> docs) throws SolrServerException, IOException {
        logger.error("Rollback documents and documents number of this Solr client is {}. ", docs.size());
        cloudSolrServer.rollback();
    }

    private <T> boolean resubmit(final Collection<T> docs, final int retryTimes) throws SolrServerException, IOException {
        logger.error("Retry time: {}", retryTimes);
        boolean submitOk = false;
        final int nextTimes = retryTimes - 1;
        if (nextTimes >= 0) {
            submitOk = addDocs(docs, nextTimes);

        } else {
            cacheService.addDocs2Cache(docs);
        }
        return submitOk;
    }

    public void close() {
        if (cloudSolrServer != null) {
            cloudSolrServer.shutdown();
        }
        if (cacheService != null) {
            cacheService.close();
        }
    }

    private <T> void addBeanByRequest(final Collection<T> docList, final int retryTime) throws SolrServerException, IOException {
        final DocumentObjectBinder binder = cloudSolrServer.getBinder();
        final ArrayList<SolrInputDocument> docs = new ArrayList<SolrInputDocument>(docList.size());
        for (final Object bean : docList) {
            docs.add(binder.toSolrInputDocument(bean));
        }
        final UpdateRequest request = new UpdateRequest();
        //when resubmit, need to open overwrite to avoid duplicate record
        if (retryTime < solrPara.getResubmitTime()) {
            request.setParam(UpdateParams.OVERWRITE, OVER_WRITE_TRUE);
        } else {
            request.setParam(UpdateParams.OVERWRITE, solrPara.getSolrOverwrite());
        }
        request.add(docs);
        request.process(cloudSolrServer);
    }

    private boolean isCommonException(final Exception exception) {
        final Throwable rootCause = SolrException.getRootCause(exception);
        return (rootCause instanceof ConnectException || rootCause instanceof ConnectTimeoutException || rootCause instanceof NoHttpResponseException || rootCause instanceof SocketException);
    }

    private HttpClient createHttpClient() {
        final HttpClientBuilder httpBuilder = HttpClientBuilder.create();
        final Builder socketConfig = SocketConfig.custom();
        socketConfig.setSoReuseAddress(true);
        socketConfig.setSoTimeout(solrPara.getHttpSocketTimeout());
        httpBuilder.setDefaultSocketConfig(socketConfig.build());
        httpBuilder.setMaxConnTotal(solrPara.getHttpMaxConnections());
        httpBuilder.setMaxConnPerRoute(solrPara.getHttpMaxConnectionsPerHost());
        httpBuilder.disableRedirectHandling();
        httpBuilder.useSystemProperties();

        final HttpRequestRetryHandler retryHandler = new HttpRequestRetryHandler() {
            @Override
            public boolean retryRequest(final IOException exception, final int executionCount, final HttpContext context) {
                // retry a max of 2 times
                if (executionCount >= MAX_EXECUTION_COUNT) {
                    return false;
                }
                final boolean wasCommError = isCommonException(exception);
                if (wasCommError) {
                    return true;
                }
                return false;
            }
        };

        httpBuilder.setRetryHandler(retryHandler);
        return httpBuilder.build();
    }

    private void initCloudSolrServer(final HttpClient httpClient) {
        final String[] servers = new String[0];
        final LBHttpSolrServer lbServer = new LBHttpSolrServer(httpClient, servers);
        cloudSolrServer = new CloudSolrServer(solrPara.getZkQuorum(), lbServer);
        cloudSolrServer.setZkClientTimeout(solrPara.getZkClientTimeout());
        cloudSolrServer.setZkConnectTimeout(solrPara.getZkConnectionTimeout());
        cloudSolrServer.setParallelUpdates(true);
        final RequestWriter requestWriter = new BinaryRequestWriter();
        cloudSolrServer.setRequestWriter(requestWriter);
    }

}
