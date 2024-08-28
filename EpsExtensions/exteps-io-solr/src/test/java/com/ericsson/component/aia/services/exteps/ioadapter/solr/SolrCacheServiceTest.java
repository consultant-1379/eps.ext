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

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.*;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.ericsson.component.aia.services.exteps.ioadapter.solr.service.SolrCacheService;
import com.ericsson.component.aia.services.exteps.ioadapter.solr.service.SolrIndexService;

/**
 * @author esarlag
 * 
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(SolrIndexService.class)
public class SolrCacheServiceTest {

    static SolrCacheService<Object> cacheService;

    static SolrOutputParameter solrPara;

    static SolrIndexService solrIndexService;

    final static long CACHE_SIZE = 7;

    @BeforeClass
    public static void init() throws ReflectiveOperationException, RuntimeException {

        solrIndexService = PowerMockito.mock(SolrIndexService.class);

        final Properties properties = SolrTestUtil.fillProperties("127.0.0.1:1266", "collection1", "true");
        properties.put(SolrTestUtil.BATCH_SIZE, "5");
        properties.put(SolrTestUtil.CACHE_SIZE, String.valueOf(CACHE_SIZE));
        solrPara = new SolrOutputParameter();
        solrPara.parameterCollect(new TestSolrOutputEventHandlerContext(properties).getEventHandlerConfiguration());

    }

    @Before
    public void cleanCache() throws InterruptedException {

        cacheService = new SolrCacheService<Object>(solrIndexService, solrPara.getBatchSize(), solrPara.getCacheSize());

    }

    @Test
    public void test_addDocs2Cache_validCollection() {
        final List<String> docs = Arrays.asList("event1", "event2", "event3", "event4", "event5");
        cacheService.addDocs2Cache(docs);

        final BlockingQueue<?> cache = Whitebox.getInternalState(cacheService, "cache");
        Assert.assertNotNull("Internal cache is null", cache);
        Assert.assertEquals("Internal cache size is wrong", cache.size(), docs.size());
    }

    @Test
    public void test_process_validInput() {

        final String object = "event";
        final BlockingQueue<String> cache = new ArrayBlockingQueue<String>((int) CACHE_SIZE);
        cache.add(object);
        Whitebox.setInternalState(cacheService, "cache", cache);

        ExecutorService newSingleThreadExecutor = Whitebox.getInternalState(cacheService, "newSingleThreadExecutor");
        if (newSingleThreadExecutor.isShutdown()) {
            newSingleThreadExecutor = Executors.newSingleThreadExecutor();
        }

        Assert.assertTrue("Object not cached", cache.contains(object));
        cacheService.process();

        Assert.assertFalse("Cache service is not shutDown", newSingleThreadExecutor.isShutdown());
    }

    @Test
    public void test_process_exceedSize() throws IllegalArgumentException, IllegalAccessException {

        final List<String> object = Arrays.asList("event1", "event2", "event3", "event4", "event5", "event6", "event7", "event8");
        cacheService.addDocs2Cache(object);

        final Set<Field> fields = Whitebox.getAllStaticFields(SolrCacheService.class);
        AtomicLong missCount = null;
        for (final Field field : fields) {
            if (field.getName().equalsIgnoreCase("missCount")) {
                missCount = (AtomicLong) field.get(cacheService);
                break;
            }
        }

        Assert.assertNotNull("Failed to retrieve counter in CacheService", missCount);
        Assert.assertTrue("There are no missed events", missCount.get() > 0);
        Assert.assertEquals("There is wrong number of missed events", missCount.get(), object.size() - CACHE_SIZE);

    }

    @Test
    public void test_processInternal_validInput() throws Exception {

        final BlockingQueue<String> cache = Whitebox.getInternalState(cacheService, "cache");
        final String object = "event";
        cache.add(object);

        final List<String> docs = new ArrayList<String>();
        final String event = "event1";
        docs.add(event);
        final boolean isBlocking = true;
        final boolean isBlocked = Whitebox.<Boolean> invokeMethod(cacheService, "process", docs, isBlocking);
        Assert.assertFalse("Not blocked", isBlocked);
        Assert.assertTrue("Docs should have been cleared", docs.isEmpty());
        Assert.assertFalse("Object not cached", cache.contains(event));
    }

    @Test
    public void test_processInternal_exceedSize() throws Exception {

        final BlockingQueue<String> cache = Whitebox.getInternalState(cacheService, "cache");
        final String object = "event";
        cache.add(object);

        final List<String> docs = new ArrayList<String>();
        final String event = "event";
        for (int i = 0; i < CACHE_SIZE + 1; i++) {
            docs.add(event + i);
        }
        final boolean isBlocking = true;
        final boolean isBlocked = Whitebox.<Boolean> invokeMethod(cacheService, "process", docs, isBlocking);
        Assert.assertFalse("Not blocked", isBlocked);

        Assert.assertTrue("Docs should have been cleared", docs.isEmpty());
        Assert.assertFalse("Object not cached", cache.contains(event + 1));
    }

    @Test
    public void test_addDocsInternal_exceedSize() throws Exception {

        final BlockingQueue<String> cache = Whitebox.getInternalState(cacheService, "cache");
        final String object = "event";
        for (int i = 0; i < CACHE_SIZE - 2; i++) {
            cache.add(object + i);
        }

        final List<String> docs = new ArrayList<String>();
        final String event = "event";
        for (int i = 0; i < 1; i++) {
            docs.add(event + i);
        }

        Whitebox.<Void> invokeMethod(cacheService, "addDocs", docs);
        Assert.assertEquals("Docs not added", CACHE_SIZE - 1, docs.size());

    }

    @Test
    public void test_addDocsInternal_validInput() throws Exception {

        final BlockingQueue<String> cache = Whitebox.getInternalState(cacheService, "cache");
        final String object = "event";
        cache.add(object);

        final List<String> docs = new ArrayList<String>();
        final String event = "event";
        for (int i = 0; i < CACHE_SIZE - 2; i++) {
            docs.add(event + i);
        }

        Whitebox.<Void> invokeMethod(cacheService, "addDocs", docs);

        Assert.assertEquals("Docs not added", docs.size(), CACHE_SIZE - 1);

    }

    @After
    public void stopServices() throws InterruptedException {
        final BlockingQueue<String> cache = Whitebox.getInternalState(cacheService, "cache");
        cache.clear();
        final ExecutorService newSingleThreadExecutor = Whitebox.getInternalState(cacheService, "newSingleThreadExecutor");
        newSingleThreadExecutor.shutdownNow();
        newSingleThreadExecutor.awaitTermination(1000, TimeUnit.MILLISECONDS);
        Assert.assertTrue("Cache service is not shutDown", newSingleThreadExecutor.isShutdown());
    }

    @AfterClass
    public static void test_closeService() throws InterruptedException {

        cacheService.close();

        final ExecutorService newSingleThreadExecutor = Whitebox.getInternalState(cacheService, "newSingleThreadExecutor");
        newSingleThreadExecutor.awaitTermination(1000, TimeUnit.MILLISECONDS);
        Assert.assertTrue("Cache service is not shutDown", newSingleThreadExecutor.isShutdown());
    }

}
