package com.ericsson.component.aia.services.exteps.io.adapter.ipl;

import com.ericsson.component.aia.itpf.common.config.Configuration;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.component.aia.services.eps.coordination.EpsAdaptiveConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.powermock.reflect.Whitebox;
import java.util.Arrays;
import java.util.Collection;

import static com.ericsson.component.aia.services.exteps.io.adapter.common.Constants.GLOBAL_PROPERTIES_HOME;
import static com.ericsson.component.aia.services.exteps.io.adapter.common.Constants.GLOBAL_PROPERTIES_TEST_FILE;
import static com.ericsson.component.aia.services.exteps.io.adapter.ipl.TestUtils.createTestConfiguration;
import static org.junit.Assert.assertNotNull;

@RunWith(Parameterized.class)
public class BatchInputAdapterTest<T> {

    private final BatchInputAdapter<T> inputAdapter;

    @After
    public void destroy() {
        inputAdapter.destroy();
    }

    public BatchInputAdapterTest(final BatchInputAdapter<T> inputAdapter){
        this.inputAdapter = inputAdapter;
        EventHandlerContext stubbedEventHandlerContext = new StubbedContext(new EpsAdaptiveConfiguration());
        Whitebox.setInternalState(inputAdapter, "eventHandlerContext", stubbedEventHandlerContext);
    }

    @Parameterized.Parameters
    public static Collection<BatchInputAdapter> genericInputAdapterImplementations() {
        return Arrays.asList(new BatchInputAdapter[] { new BatchStubbedAvroInputAdapter(), new BatchStubbedRawInputAdapter()});
    }

    @Before
    public void setUp() {
        System.setProperty(GLOBAL_PROPERTIES_HOME, GLOBAL_PROPERTIES_TEST_FILE);
        inputAdapter.doInit();
    }

    @Test
    public void testSubscriberCreatedSuccessfully() {
        assertNotNull(inputAdapter.getSubscriber());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testOnEvent() {
        inputAdapter.onEvent("");
    }
}


class BatchStubbedRawInputAdapter extends BatchInputAdapter<byte[]> {
    @Override
    public Configuration getConfig() {
        return createTestConfiguration("RAW_SUBSCRIBER_INTEGRATION_POINT");
    }
}

class BatchStubbedAvroInputAdapter extends BatchInputAdapter<byte[]> {
    @Override
    public Configuration getConfig() {
        return createTestConfiguration("AVRO_SUBSCRIBER_INTEGRATION_POINT");
    }
}

