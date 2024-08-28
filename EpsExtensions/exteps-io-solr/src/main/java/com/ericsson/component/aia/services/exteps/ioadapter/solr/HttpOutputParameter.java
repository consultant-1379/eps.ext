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

public class HttpOutputParameter {

    public int httpConnectionTimeout;
    public int httpSocketTimeout;
    public int httpMaxConnections;
    public int httpMaxConnectionsPerHost;

    public boolean isHttpParamsValid() {
        if (httpConnectionTimeout < 0 || httpSocketTimeout < 0 || httpMaxConnections <= 0 || httpMaxConnectionsPerHost <= 0) {
            return false;
            //SolrParameter.;
        }
        return true;
    }

}