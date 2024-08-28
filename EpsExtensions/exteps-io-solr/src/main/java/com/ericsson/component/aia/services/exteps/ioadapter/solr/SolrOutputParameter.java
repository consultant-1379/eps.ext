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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.itpf.common.config.Configuration;

public class SolrOutputParameter extends SolrParameter {

    private static final Logger logger = LoggerFactory.getLogger(SolrOutputParameter.class);

    private String zkQuorum;
    private int zkClientTimeout;
    private int zkConnectionTimeout;
    private String solrCollection;
    private boolean solrClientCommit;
    private boolean waitFlush;
    private boolean waitSearcher;
    private boolean softCommit;

    private int resubmitTime;
    private int batchSize;
    private int cacheSize;

    private String solrOverwrite;
    private final HttpOutputParameter httpParams = new HttpOutputParameter();
    private Integer retentionDay;
    private String timeZone;

    public void parameterCheck() {

        validateZkQuorumParameter();
        validateExceptionHandlerParameter();

        validatePositiveParams();

        if (batchSize > cacheSize) {
            throwValidationException("Cache size should be greater than or equal batch size.");
        }

        validateOverwriteParams();
        if (!httpParams.isHttpParamsValid()) {
            throwValidationException("HTTP client parameter is invalid.");
        }
        validateBuildersParameters();

        if (retentionDay != null && retentionDay.intValue() <= 0) {
            throwValidationException("Retention day is not a valid integer. ");
        }

        if (retentionDay != null && timeZone.isEmpty()) {
            throwValidationException("Time zone is invalid. ");
        }

    }

    @Override
    public void parameterCollect(final Configuration config) {

        super.parameterCollect(config);

        String zkQuorum = config.getStringProperty(SolrOutputAdapterData.ZK_QUORUM_PARAM_NAME);
        //read zkQuorum by system property
        if (zkQuorum == null || zkQuorum.trim().isEmpty() || zkQuorum.contains(SolrOutputAdapterData.SYS_SOLR_ALIAS)) {
            zkQuorum = System.getProperty(SolrOutputAdapterData.SYS_SOLR_ALIAS);
        }
        this.zkQuorum = zkQuorum;
        zkClientTimeout = getIntConfigParamOrDefault(config, SolrOutputAdapterData.ZK_CLIENT_TIMEOUT_PARAM_NAME,
                SolrOutputAdapterData.DEFAULT_ZK_CLIENT_TIMEOUT);
        zkConnectionTimeout = getIntConfigParamOrDefault(config, SolrOutputAdapterData.ZK_CONNECTION_TIMEOUT_PARAM_NAME,
                SolrOutputAdapterData.DEFAULT_ZK_CONNECTION_TIMEOUT);
        setSolrCollection(config.getStringProperty(SolrOutputAdapterData.SOLR_COLLECTION));
        solrClientCommit = getBooleanConfigParamOrDefault(config, SolrOutputAdapterData.SOLR_CLIENT_COMMIT,
                SolrOutputAdapterData.DEFAULT_SOLR_CLIENT_COMMIT);
        waitFlush = getBooleanConfigParamOrDefault(config, SolrOutputAdapterData.SOLR_COMMIT_WAIT_FLUSH,
                SolrOutputAdapterData.DEFAULT_SOLR_COMMIT_WAIT_FLUSH);
        waitSearcher = getBooleanConfigParamOrDefault(config, SolrOutputAdapterData.SOLR_COMMIT_WAIT_SEARCHER,
                SolrOutputAdapterData.DEFAULT_SOLR_COMMIT_WAIT_SEARCHER);
        softCommit = getBooleanConfigParamOrDefault(config, SolrOutputAdapterData.SOLR_COMMIT_SOFT_COMMIT,
                SolrOutputAdapterData.DEFAULT_SOLR_COMMIT_SOFT_COMMIT);
        exceptionHandler = getStringConfigParamOrDefault(config, SolrOutputAdapterData.SOLR_COMMIT_EXCEPTION_HANDLER,
                SolrOutputAdapterData.DEFAULT_SOLR_COMMIT_EXCEPTION_HANDLER_RESUBMIT);
        resubmitTime = getIntConfigParamOrDefault(config, SolrOutputAdapterData.SOLR_RESUBMIT_TIME, SolrOutputAdapterData.DEFAULT_SOLR_RESUBMIT_TIME);
        batchSize = getIntConfigParamOrDefault(config, SolrOutputAdapterData.BATCH_SIZE, SolrOutputAdapterData.DEFAULT_BATCH_SIZE);
        cacheSize = getIntConfigParamOrDefault(config, SolrOutputAdapterData.CACHE_SIZE, SolrOutputAdapterData.DEFAULT_CACHE_SIZE);

        solrOverwrite = getStringConfigParamOrDefault(config, SolrOutputAdapterData.SOLR_OVERWRITE, SolrOutputAdapterData.DEFAULT_SOLR_OVERWRITE);
        httpParams.httpConnectionTimeout = getIntConfigParamOrDefault(config, SolrOutputAdapterData.HTTP_CONNECTION_TIMEOUT,
                SolrOutputAdapterData.DEFAULT_HTTP_CONNECTION_TIMEOUT);
        httpParams.httpSocketTimeout = getIntConfigParamOrDefault(config, SolrOutputAdapterData.HTTP_SOCKET_TIMEOUT,
                SolrOutputAdapterData.DEFAULT_HTTP_SOCKET_TIMEOUT);
        httpParams.httpMaxConnections = getIntConfigParamOrDefault(config, SolrOutputAdapterData.HTTP_MAX_CONNECTIONS,
                SolrOutputAdapterData.DEFAULT_HTTP_MAX_CONNECTIONS);
        httpParams.httpMaxConnectionsPerHost = getIntConfigParamOrDefault(config, SolrOutputAdapterData.HTTP_MAX_CONNECTIONS_PER_HOST,
                SolrOutputAdapterData.DEFAULT_HTTP_MAX_CONNECTIONS_PER_HOST);
        retentionDay = config.getIntProperty(SolrOutputAdapterData.RETENTION_DAY);
        timeZone = config.getStringProperty(SolrOutputAdapterData.TIME_ZONE);
    }

    public void validatePositiveParams() {
        if (resubmitTime < 0 || batchSize < 0 || cacheSize < 0) {
            throwValidationException("Resubmit time and batch size and cache size should be greater than 0. ");
        }
    }

    public void validateExceptionHandlerParameter() {
        if (!solrClientCommit && exceptionHandler.equalsIgnoreCase(SolrOutputAdapterData.DEFAULT_SOLR_COMMIT_EXCEPTION_HANDLER_ROLLBACK)) {
            throwValidationException("Only client-commit supports the exception handler rollback.");
        }
    }

    public void validateZkQuorumParameter() {
        if (zkQuorum == null || zkQuorum.trim().isEmpty()) {
            throwValidationException("zkQuorum must not be null or empty");
        }
    }

    public void validateOverwriteParams() {
        if (solrOverwrite != null && !solrOverwrite.equals(SolrOutputAdapterData.DEFAULT_SOLR_OVERWRITE)
                && !solrOverwrite.equals(SolrOutputAdapterData.SOLR_OVERWRITE_FALSE)) {
            throwValidationException("Overwrite should be either true or false. ");
        }
    }

    public void validateBuildersParameters() {
        if ((solrCollection == null || solrCollection.trim().isEmpty()) && solrCollectionBuilder == null) {
            SolrOutputParameter.throwValidationException("Both Solr collection and Solr collection builder are invalid.");
        }
    }

    private int getIntConfigParamOrDefault(final Configuration config, final String configParamName, final int defaultValue) {

        final String numAsString = config.getStringProperty(configParamName);

        if (numAsString != null && !numAsString.trim().isEmpty()) {
            logger.debug("Found {} = {}. Will try to parse it to integer value", configParamName, numAsString);

            try {
                final int val = Integer.parseInt(numAsString);
                logger.info("{} set to configured value {}", configParamName, val);
                return val;
            } catch (final Exception e) {
                logger.error("Exception while parsing {}. Details {}. Will use default value {}", configParamName, e.getMessage(), defaultValue);
            }
        }

        logger.info("{} set to default value {}", configParamName, defaultValue);
        return defaultValue;
    }

    private boolean getBooleanConfigParamOrDefault(final Configuration config, final String configParamName, final boolean defaultValue) {

        final Boolean strAsBoolean = config.getBooleanProperty(configParamName);

        if (strAsBoolean != null) {
            logger.info("Found {} = {}. ", configParamName, strAsBoolean);

            return strAsBoolean;
        }

        logger.info("{} set to default value {}", configParamName, defaultValue);
        return defaultValue;
    }

    protected String getStringConfigParamOrDefault(final Configuration config, final String configParamName, final String defaultValue) {

        final String stringValue = config.getStringProperty(configParamName);

        if (stringValue != null && !stringValue.trim().isEmpty()) {
            logger.info("Found {} = {}. ", configParamName, stringValue);
            return stringValue;
        }

        logger.info("{} set to default value {}", configParamName, defaultValue);
        return defaultValue;
    }

    public String getSolrCollection() {
        return solrCollection;
    }

    public void setSolrCollection(final String solrCollection) {
        this.solrCollection = solrCollection;
    }

    public String getZkQuorum() {
        return zkQuorum;
    }

    public void setZkQuorum(final String zkQuorum) {
        this.zkQuorum = zkQuorum;
    }

    public int getZkClientTimeout() {
        return zkClientTimeout;
    }

    public void setZkClientTimeout(final int zkClientTimeout) {
        this.zkClientTimeout = zkClientTimeout;
    }

    public int getZkConnectionTimeout() {
        return zkConnectionTimeout;
    }

    public void setZkConnectionTimeout(final int zkConnectionTimeout) {
        this.zkConnectionTimeout = zkConnectionTimeout;
    }

    public boolean isWaitFlush() {
        return waitFlush;
    }

    public void setWaitFlush(final boolean waitFlush) {
        this.waitFlush = waitFlush;
    }

    public boolean isWaitSearcher() {
        return waitSearcher;
    }

    public void setWaitSearcher(final boolean waitSearcher) {
        this.waitSearcher = waitSearcher;
    }

    public String getExceptionHandler() {
        return exceptionHandler;
    }

    public void setExceptionHandler(final String exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    public int getResubmitTime() {
        return resubmitTime;
    }

    public void setResubmitTime(final int resubmitTime) {
        this.resubmitTime = resubmitTime;
    }

    public Integer getRetentionDay() {
        return retentionDay;
    }

    public void setRetentionDay(final Integer retentionDay) {
        this.retentionDay = retentionDay;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(final int batchSize) {
        this.batchSize = batchSize;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(final int cacheSize) {
        this.cacheSize = cacheSize;
    }

    public boolean isSolrClientCommit() {
        return solrClientCommit;
    }

    public void setSolrClientCommit(final boolean solrClientCommit) {
        this.solrClientCommit = solrClientCommit;
    }

    public boolean isSoftCommit() {
        return softCommit;
    }

    public void setSoftCommit(final boolean softCommit) {
        this.softCommit = softCommit;
    }

    public String getSolrOverwrite() {
        return solrOverwrite;
    }

    public void setSolrOverwrite(final String solrOverwrite) {
        this.solrOverwrite = solrOverwrite;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(final String timeZone) {
        this.timeZone = timeZone;
    }

    public int getHttpSocketTimeout() {
        return httpParams.httpSocketTimeout;
    }

    public int getHttpMaxConnections() {
        return httpParams.httpMaxConnections;
    }

    public int getHttpMaxConnectionsPerHost() {
        return httpParams.httpMaxConnectionsPerHost;
    }

}