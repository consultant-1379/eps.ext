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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolrCacheService<D> {

    private static AtomicLong missCount = new AtomicLong();

    private static final int LOG_COUNT = 1000;
    private static final int SLEEP_TIME = 5000;

    private final Logger logger = LoggerFactory.getLogger(SolrCacheService.class);

    private final BlockingQueue<D> cache;

    private final int batchSize;
    private final SolrIndexService indexService;
    private final ExecutorService newSingleThreadExecutor = Executors.newSingleThreadExecutor();

    public SolrCacheService(final SolrIndexService indexServ, final int batch, final int size) {
        cache = new ArrayBlockingQueue<D>(size, true);
        batchSize = batch;
        indexService = indexServ;
    }

    public void process() {

        newSingleThreadExecutor.submit(new Runnable() {

            @Override
            public void run() {
                final List<D> docs = new ArrayList<D>();
                boolean isBlocking = false;
                while (true) {
                    isBlocking = process(docs, isBlocking);
                }
            }

        });
    }

    @SuppressWarnings("unchecked")
    public synchronized void addDocs2Cache(final Collection<?> docs) {
        final int remainingCapacity = cache.remainingCapacity();
        final int requiredCapacity = docs.size();
        if (remainingCapacity < requiredCapacity) {
            for (int i = 0; i < requiredCapacity - remainingCapacity; i++) {
                if (cache.poll() != null) {
                    missCount.incrementAndGet();
                }
            }
        }
        for (final Object doc : docs) {
            if (!cache.offer((D) doc)) {
                missCount.incrementAndGet();
            }
        }
        if (logger.isInfoEnabled()) {
            final long num = missCount.get();
            if (num != 0 && num % LOG_COUNT == 0) {
                logger.info("Discarded event number is {}.", num);
            }
        }
    }

    public void close() {
        newSingleThreadExecutor.shutdown();
    }

    private boolean process(final List<D> docs, boolean isBlocking) {
        D doc = null;
        try {
            if (isBlocking) {
                Thread.sleep(SLEEP_TIME);
                isBlocking = false;
            } else {
                //block when no element
                doc = cache.take();

                docs.add(doc);
                addDocs(docs);
                if (!docs.isEmpty()) {
                    isBlocking = !(indexService.addDocs(docs));
                }
            }
        } catch (final InterruptedException ie) {
            logger.error("InterruptedException happened.", ie);
        } catch (SolrServerException | IOException e) {
        } finally {
            if (!docs.isEmpty()) {
                docs.clear();
            }
        }
        return isBlocking;
    }

    private void addDocs(final List<D> docs) {
        final int arrayNum = batchSize > cache.size() ? batchSize : cache.size();
        for (int i = 0; i < arrayNum; i++) {
            final D element = cache.poll();
            if (element != null) {
                docs.add(element);
            }
        }
    }
}
