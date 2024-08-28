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
package com.ericsson.component.aia.services.exteps.ioadapter.solr;

import java.io.IOException;
import java.util.*;

import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Meter;
import com.ericsson.component.aia.services.exteps.ioadapter.solr.service.SolrIndexService;
import com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler;
import com.ericsson.component.aia.services.eps.EpsEngineConstants;
import com.ericsson.component.aia.itpf.common.io.OutputAdapter;
import com.ericsson.component.aia.services.eps.statistics.EpsStatisticsRegister;

public class SolrOutputAdapter extends AbstractEventHandler implements OutputAdapter {

    public final SolrOutputAdapterData data = new SolrOutputAdapterData();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Meter solrRecordsReceived;
    private Meter solrRecordsIndexed;

    private SolrOutputParameter solrPara;

    private EpsStatisticsRegister statisticsRegister;

    private boolean statisticsOn;

    private SolrIndexService indexService;

    @Override
    public boolean understandsURI(final String uri) {
        if (uri == null) {
            return false;
        }
        return uri.equals(SolrOutputAdapterData.URI);
    }

    @Override
    public void onEvent(final Object inputEvent) {
        Date startTime = null;
        if (logger.isDebugEnabled()) {
            startTime = new Date();
        }
        int indexCount = 0;
        try {
            Collection<Object> eventCollection = null;
            if (inputEvent instanceof Collection<?>) {
                eventCollection = (Collection<Object>) inputEvent;
            } else {
                eventCollection = new ArrayList<Object>();
                eventCollection.add(inputEvent);
            }
            indexCount = sendEventsToSolr(eventCollection);
        } catch (final Exception e) {
            log.error("Exception happened in onEvent method.", e);
        } finally {
            if (logger.isDebugEnabled()) {
                final Date finishTime = new Date();
                logger.debug("Incoming event statistics: {} events were handled within {} ms", indexCount, finishTime.getTime() - startTime.getTime());
            }
        }
    }

    private <T> int sendEventsToSolr(final Collection<?> inputEventCollection) throws SolrServerException, IOException {
        int numDocsAdded = 0;
        numDocsAdded = inputEventCollection.size();
        addRecordsToReceivedMeter(numDocsAdded);
        if (indexService.addDocs(inputEventCollection)) {
            addRecordsToIndexedMeter(numDocsAdded);
            logger.debug("successfully indexed {} documents.", numDocsAdded);
        }
        return numDocsAdded;
    }

    @Override
    protected void doInit() {

        solrPara = new SolrOutputParameter();
        solrPara.parameterCollect(getConfiguration());

        solrPara.parameterCheck();

        indexService = SolrIndexService.getInstance(solrPara);

        initialiseStatistics();
    }

    /**
     * Initialise statistics.
     */
    protected void initialiseStatistics() {
        statisticsRegister = (EpsStatisticsRegister) getEventHandlerContext().getContextualData(
                EpsEngineConstants.STATISTICS_REGISTER_CONTEXTUAL_DATA_NAME);
        if (statisticsRegister == null) {
            log.error("Failed to initialize EpsStatisticsRegister ...");
        } else {
            statisticsOn = statisticsRegister.isStatisticsOn();
            if (statisticsOn) {
                solrRecordsReceived = statisticsRegister.createMeter(SolrOutputAdapterData.EPS_EXT + data.SOLR_OUTPUT_ADAPTER
                        + SolrOutputAdapterData.SOLR_RECORDS_RECEIVED);
                solrRecordsIndexed = statisticsRegister.createMeter(SolrOutputAdapterData.EPS_EXT + data.SOLR_OUTPUT_ADAPTER
                        + SolrOutputAdapterData.SOLR_RECORDS_INDEXED);
            }
        }
    }

    private void addRecordsToReceivedMeter(final int numRecords) {
        if (statisticsOn) {
            solrRecordsReceived.mark(numRecords);
        }
    }

    private void addRecordsToIndexedMeter(final int numRecords) {
        if (statisticsOn) {
            solrRecordsIndexed.mark(numRecords);
        }
    }

}
