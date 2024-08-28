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

import static org.mockito.Matchers.isA;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.ericsson.component.aia.services.exteps.ioadapter.solr.SolrOutputAdapter;
import com.ericsson.component.aia.services.exteps.ioadapter.solr.SolrOutputParameter;
import com.ericsson.component.aia.services.exteps.ioadapter.solr.service.SolrIndexService;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.component.aia.services.eps.EpsConfigurationConstants;

/**
 * @author esarlag
 * 
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(SolrIndexService.class)
public class SolrOutputAdapterTest {

    static final String EXPECTED_URI = "solr:/";

    static SolrOutputAdapter solrOutputAdapter;

    @Mock
    EventHandlerContext mockEventHandlerContext;

    @Mock
    SolrOutputParameter mockStreamingConfig;

    @Mock
    CloudSolrServer mockCloudSolrServer;

    @Mock
    static DocumentObjectBinder mockDocObjectBinder = PowerMockito.mock(DocumentObjectBinder.class);

    @BeforeClass
    public static void doInitMock() throws Exception {

        System.setProperty(EpsConfigurationConstants.STATISTICS_OFF_SYS_PARAM_NAME, "false");

        final Properties properties = SolrTestUtil.fillProperties("zkQuorumParam", "solrCollection", "true");
        final TestSolrOutputEventHandlerContext ctx = new TestSolrOutputEventHandlerContext(properties);

        final CloudSolrServer mockCloudSolrServer = PowerMockito.mock(CloudSolrServer.class);
        PowerMockito.whenNew(CloudSolrServer.class).withArguments(isA(String.class), isA(LBHttpSolrServer.class)).thenReturn(mockCloudSolrServer);
        Mockito.doReturn(mockDocObjectBinder).when(mockCloudSolrServer).getBinder();
        final String string = "";
        Mockito.doReturn(new SolrInputDocument()).when(mockDocObjectBinder).toSolrInputDocument(string);

        solrOutputAdapter = new SolrOutputAdapter();
        solrOutputAdapter.init(ctx);

    }

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void understandsUri_validUri() {

        final boolean result = solrOutputAdapter.understandsURI(EXPECTED_URI);
        Assert.assertTrue("Failed to recognize valid URI " + EXPECTED_URI, result);

    }

    @Test
    public void understandsUri_nullUri() {

        final boolean result = solrOutputAdapter.understandsURI(null);
        Assert.assertFalse("Failed negative test with null URI ", result);

    }

    @Test
    public void understandsUri_invalidUri() {

        final boolean result = solrOutputAdapter.understandsURI("invalidUri");
        Assert.assertFalse("Failed negative test with invalid URI", result);

    }

    @Test
    public void onEvent_singleEvent() throws Exception {

        final SolrIndexService indexService = extractIndexService();
        final long initialDocsNumber = extractDocsNumber(indexService);

        final String inputEvent = "event";
        solrOutputAdapter.onEvent(inputEvent);
        final long finalDocsNumber = extractDocsNumber(indexService);

        Assert.assertEquals("Docs not added", initialDocsNumber + 1L, finalDocsNumber);
    }

    @Test
    public void onEvent_collectionEvent() throws Exception {

        final SolrIndexService indexService = extractIndexService();
        final long initialDocsNumber = extractDocsNumber(indexService);

        final List<String> events = Arrays.asList("event1", "event2", "event3", "event4", "event5");
        solrOutputAdapter.onEvent(events);
        final long finalDocsNumber = extractDocsNumber(indexService);

        Assert.assertEquals("Docs not added", initialDocsNumber + events.size(), finalDocsNumber);
    }

    @Test
    public void onEvent_nullEvent() throws Exception {

        final SolrIndexService indexService = extractIndexService();
        final long initialDocsNumber = extractDocsNumber(indexService);

        solrOutputAdapter.onEvent(null);
        final long finalDocsNumber = extractDocsNumber(indexService);

        Assert.assertEquals("Docs not added", initialDocsNumber + 1L, finalDocsNumber);
    }

    private long extractDocsNumber(final SolrIndexService indexService) {
        final long docsNumber = ((AtomicLong) Whitebox.getInternalState(indexService, "totalCount")).get();
        Assert.assertNotNull(docsNumber);
        return docsNumber;
    }

    private SolrIndexService extractIndexService() {
        final SolrIndexService indexService = (SolrIndexService) Whitebox.getInternalState(solrOutputAdapter, "indexService");
        Assert.assertNotNull(indexService);
        return indexService;
    }
}
