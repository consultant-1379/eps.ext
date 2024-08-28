/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.exteps.io.adapter.streaming;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ericsson.component.aia.mediation.parsers.streamrecord.StreamedRecord;
import com.ericsson.component.aia.services.eps.coordination.EpsAdaptiveConfiguration;
import com.ericsson.component.aia.services.eps.core.EpsConstants;
import com.ericsson.component.aia.services.eps.core.modules.ModuleManager;
import com.ericsson.component.aia.services.eps.core.util.EpsUtil;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.utils.AbstractZookeeperTest;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.utils.EpsTestUtil;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.utils.MockObserver;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.utils.ReactiveStreamingInputAdapterExt;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.utils.TestingSingleton;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.utils.torserver.TORStreamingServer;
import com.ericsson.oss.itpf.sdk.cluster.coordination.Layer;
import com.ericsson.oss.itpf.sdk.cluster.coordination.application.Application;
import com.ericsson.oss.itpf.sdk.cluster.coordination.application.ApplicationFactory;
import com.ericsson.oss.itpf.sdk.cluster.coordination.application.Node;
import com.ericsson.oss.itpf.sdk.cluster.coordination.application.NodeObserverType;

import static com.ericsson.component.aia.services.eps.core.coordination.CoordinationUtil.getQualifiedEpsName;

public class TestConfigChangeThroughZookeeper extends AbstractZookeeperTest {

    private static final String INPUT_IP = "inputIP";
    private static final String INPUT_PORT = "inputPort";
    private static final String USER_ID = "userId";
    private static final String FILTER_ID = "FilterId";
    private static final String GROUP_ID = "GroupId";
    private static final String STREAM_LOAD_MONITOR = "StreamLoadMonitor";
    private static final String MONITOR_PERIOD = "MonitorPeriod";
    private static final String MONITORING_INTERVAL = "monitoringInterval";
    private static final TestingSingleton testingSingleton = TestingSingleton.getInstance();
    private static final byte[] recordDataOnPort10866 = new byte[] { 0x00, 0x00, 0x00, 0x01, 0x00, 0x0A, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
            0x08 };
    private static final byte[] recordDataOnPort10867 = new byte[] { 0x00, 0x00, 0x00, 0x01, 0x00, 0x0A, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
            0x09 };
    private static final int timeout = 60000;
    private static final String epsId = "eps01";

    private final EpsTestUtil epsTestUtil = new EpsTestUtil();

    @Before
    public void setup() throws InterruptedException, ExecutionException {
        System.setProperty("com.ericsson.oss.services.eps.module.deployment.folder.path", "src/test/resources");
        System.setProperty("com.ericsson.oss.services.eps.deploy.already.existing.modules.on.startup", "false");
        System.setProperty(EpsConstants.EPS_INSTANCE_ID_PROP_NAME, epsId);
        epsTestUtil.createEpsInstanceInNewThread();
    }

    @After
    public void tearDownEps() {
        epsTestUtil.shutdownEpsInstance();
    }

    @Test
    public void shouldReactAndTakeInitialAndSubsequentConfig() {
        new TORStreamingServer(10866, recordDataOnPort10866).start();
        new TORStreamingServer(10867, recordDataOnPort10867).start();
        InputStream moduleInputStream = null;
        try {
            moduleInputStream = new FileInputStream("src/test/resources/flows/flow_through_zookeeper_with_config_change.xml");
        } catch (final FileNotFoundException e) {
        }
        assertNotNull(moduleInputStream);
        final String moduleId = epsTestUtil.deployModule(moduleInputStream);

        final ModuleManager moduleManager = epsTestUtil.getEpsInstanceManager().getModuleManager();
        assertEquals(1, moduleManager.getDeployedModulesCount());

        final Application application = ApplicationFactory.get(Layer.SERVICES, "eps", moduleId,
                getQualifiedEpsName(EpsUtil.getEpsInstanceIdentifier()));
        final Node node = application.configure("streamInput");
        final EpsAdaptiveConfiguration epsAdaptiveConfiguration = new EpsAdaptiveConfiguration();
        epsAdaptiveConfiguration.setConfiguration(getTestData(10866));

        node.createOrUpdate(epsAdaptiveConfiguration);
        verifyData(recordDataOnPort10866);
        sleep(3000); //wait for TOR streaming to send the disconnect message

        epsAdaptiveConfiguration.setConfiguration(getTestData(10867));
        node.createOrUpdate(epsAdaptiveConfiguration);
        verifyData(recordDataOnPort10867);
        sleep(3000);

        epsAdaptiveConfiguration.setConfiguration(getTestData(10866));
        node.createOrUpdate(epsAdaptiveConfiguration);
        verifyData(recordDataOnPort10866);
        sleep(3000);
    }

    private void sleep(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (final InterruptedException interruptedException) {
        }
    }

    @Test
    public void shouldReactToConfiguration() {
        final Application application = ApplicationFactory.get(Layer.SERVICES, "eps", "x", "a");
        application.register();
        final Node node = application.configure("streamInput");
        final ReactiveStreamingInputAdapterExt reactiveStreamingInputAdapterExt = new ReactiveStreamingInputAdapterExt();
        final MockObserver mockObserver = new MockObserver(reactiveStreamingInputAdapterExt);
        node.observe().register(mockObserver, NodeObserverType.NODE_ONLY);

        reactiveStreamingInputAdapterExt.setControlEvent(null);
        final EpsAdaptiveConfiguration epsAdaptiveConfiguration = new EpsAdaptiveConfiguration();
        epsAdaptiveConfiguration.setConfiguration(getTestData(10860));
        node.createOrUpdate(epsAdaptiveConfiguration);
        verifyControlEventConfig(node, reactiveStreamingInputAdapterExt, getTestData(10860), getTestData(10860));
    }

    @Test
    public void shouldReactToConfigurationTwice() {
        final Application application = ApplicationFactory.get(Layer.SERVICES, "eps", "x", "b");
        application.register();
        final Node node = application.configure("streamInput");
        final ReactiveStreamingInputAdapterExt reactiveStreamingInputAdapterExt = new ReactiveStreamingInputAdapterExt();
        final MockObserver mockObserver = new MockObserver(reactiveStreamingInputAdapterExt);
        node.observe().register(mockObserver, NodeObserverType.NODE_ONLY);
        verifyControlEventConfig(node, reactiveStreamingInputAdapterExt, getTestData(10861), getTestData(10861));
        verifyControlEventConfig(node, reactiveStreamingInputAdapterExt, getTestData(10862), getTestData(10862));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionsWhenGivenNullConfig() {
        final EpsAdaptiveConfiguration epsAdaptiveConfiguration = new EpsAdaptiveConfiguration();
        epsAdaptiveConfiguration.setConfiguration(null);
    }

    public void verifyControlEventConfig(final Node node, final ReactiveStreamingInputAdapterExt reactiveStreamingInputAdapterExt,
                                         final Map<String, Object> inputData, final Map<String, Object> outputData) {
        reactiveStreamingInputAdapterExt.setControlEvent(null);
        final EpsAdaptiveConfiguration epsAdaptiveConfiguration = new EpsAdaptiveConfiguration();
        epsAdaptiveConfiguration.setConfiguration(inputData);
        node.createOrUpdate(epsAdaptiveConfiguration);
        final long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() < startTime + timeout) {
            if (reactiveStreamingInputAdapterExt.getControlEvent() != null) {
                break;
            } else {
                sleep(100);
            }
        }
        assertEquals(reactiveStreamingInputAdapterExt.getControlEvent().getData(), outputData);
    }

    private Map<String, Object> getTestData(final int port) {
        final Map<String, Object> data = new HashMap<String, Object>();
        data.put(INPUT_IP, "localhost");
        data.put(INPUT_PORT, Integer.toString(port));
        data.put(GROUP_ID, "123");
        data.put(FILTER_ID, "1");
        data.put(USER_ID, "1");
        data.put(STREAM_LOAD_MONITOR, "false");
        data.put(MONITOR_PERIOD, "10000");
        data.put(MONITORING_INTERVAL, "10000");
        return data;
    }

    private void verifyData(final byte[] data) {
        final byte[] truncatedData = Arrays.copyOfRange(data, 4, data.length); //The first 4 bytes are for identifying that it's an event message and giving the source ID and they aren't included when calling getData
        final String hexData = bytesToHex(truncatedData);
        StreamedRecord record = testingSingleton.getRecord();
        final long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() < startTime + timeout) {
            record = testingSingleton.getRecord();
            if (hexData.equals(bytesToHex(record.getData()))) {
                break;
            } else {
                sleep(100);
            }
        }
        assertEquals(hexData, bytesToHex(record.getData()));
    }

    private static String bytesToHex(final byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        final char[] hexArray = "0123456789ABCDEF".toCharArray();
        final char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            final int maskedByte = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[maskedByte >>> 4];
            hexChars[j * 2 + 1] = hexArray[maskedByte & 0x0F];
        }
        return new String(hexChars);
    }
}
