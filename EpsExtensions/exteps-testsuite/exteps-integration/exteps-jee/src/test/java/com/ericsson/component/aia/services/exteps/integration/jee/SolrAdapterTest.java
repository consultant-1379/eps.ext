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
package com.ericsson.component.aia.services.exteps.integration.jee;

import java.util.Arrays;
import java.util.List;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.ericsson.component.aia.services.exteps.ioadapter.solr.SolrOutputAdapter;
import com.ericsson.component.aia.services.exteps.ioadapter.solr.service.SolrIndexService;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;

/**
 * @author esarlag
 *
 */
@RunWith(Arquillian.class)
public class SolrAdapterTest extends CommonExtensionTest {

    private static final String testResoucesFolder = SolrAdapterTest.class
            .getSimpleName();
    private static final String epsWarCoordinates = Artifact.COM_ERICSSON_OSS_ITPF_SERVICES_EPS_WAR
            + ":?";
    private static final String manifestFile = "MANIFEST.MF";
    private static final String artifactCoordinates = "com.ericsson.oss.services.exteps:exteps-io-solr:jar:?";

    @SuppressWarnings("unchecked")
    @Deployment
    public static EnterpriseArchive createDeployment() {

        final List<?> handlerClasses = Arrays
                .asList((Class<?>) SolrOutputAdapter.class);
        final String flowFileName = testResoucesFolder
                + "/flows/valid_solr_flow.xml";
        final String epsConfigFileName = testResoucesFolder
                + "/properties/EpsConfiguration.properties";
        final String jbossDeploymentFile = testResoucesFolder
                + "/application/jboss-deployment-structure.xml";
        final List<?> ejbJarClasses = Arrays
                .asList((Class<?>) SolrOutputAdapter.class);
        final EnterpriseArchive ear = Artifact
                .buildEar(SolrAdapterTest.class.getSimpleName() + ".ear",
                        epsWarCoordinates, flowFileName, epsConfigFileName,
                        manifestFile, jbossDeploymentFile,
                        SolrAdapterTest.class, (List<Class<?>>) handlerClasses,
                        (List<Class<?>>) ejbJarClasses);

        ear.addAsLibraries(Artifact
                .resolveArtifactWithDependencies(artifactCoordinates));

        return ear;
    }

    @Test(expected = NullPointerException.class)
    @InSequence(1)
    public void testSolrNullParams() {
        SolrIndexService.getInstance(null);
    }

    @Test
    @InSequence(2)
    public void testSolr() throws InterruptedException {

        final HazelcastInstance hzInstance = Artifact.createHazelcastInstance();
        final ITopic<String> hazelcastSendTopic = hzInstance
                .getTopic("eps-topic1");

        Artifact.waitForModuleDeploy(1, 3);

        hazelcastSendTopic.publish("1");

        hzInstance.getLifecycleService().shutdown();
    }
}
