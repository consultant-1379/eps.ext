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

import com.ericsson.component.aia.services.exteps.ioadapter.solr.external.SolrCollectionBuilder;
import com.ericsson.component.aia.itpf.common.config.Configuration;

public class SolrParameter {

    private static final Logger logger = LoggerFactory.getLogger(SolrParameter.class);

    protected SolrCollectionBuilder solrCollectionBuilder;
    protected String exceptionHandler;

    public SolrCollectionBuilder getSolrCollectionBuilder() {
        return solrCollectionBuilder;
    }

    public void setSolrCollectionBuilder(final SolrCollectionBuilder solrCollectionBuilder) {
        this.solrCollectionBuilder = solrCollectionBuilder;
    }

    public void parameterCollect(final Configuration config) {
        solrCollectionBuilder = createSolrCollectionBuilder(config);
    }

    public static SolrCollectionBuilder createSolrCollectionBuilder(final Configuration config) {
        final String solrCollectionBuilderClassName = config.getStringProperty(SolrOutputAdapterData.SOLR_COLLECTION_BUILDER);
        if (solrCollectionBuilderClassName == null || solrCollectionBuilderClassName.trim().isEmpty()) {
            return null;
        }
        Class<?> clazz;
        try {
            clazz = Class.forName(solrCollectionBuilderClassName);
            if (!SolrCollectionBuilder.class.isAssignableFrom(clazz)) {
                throw new IllegalArgumentException("Class " + solrCollectionBuilderClassName + " does not implement "
                        + SolrCollectionBuilder.class.getName());
            }
        } catch (final ClassNotFoundException e) {
            throw new IllegalArgumentException("Was not able to find class by name [" + solrCollectionBuilderClassName + "]");
        }
        try {
            return (SolrCollectionBuilder) clazz.newInstance();
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static void throwValidationException(final String message) {
        logger.error(message);
        throw new IllegalArgumentException(message);
    }

}
