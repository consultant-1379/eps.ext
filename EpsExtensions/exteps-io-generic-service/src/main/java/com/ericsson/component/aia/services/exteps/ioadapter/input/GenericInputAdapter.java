package com.ericsson.component.aia.services.exteps.ioadapter.input;

import static com.google.common.base.Preconditions.*;

import java.util.Map;
import java.util.Properties;

import com.ericsson.component.aia.common.transport.service.MessageServiceTypes;
import com.ericsson.component.aia.common.transport.service.util.ProcessorThreadFactory;
import com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.component.aia.itpf.common.io.InputAdapter;
import com.ericsson.component.aia.services.eps.statistics.EpsStatisticsRegister;
import com.ericsson.component.aia.services.exteps.ioadapter.builder.IO_CONSTANTS;
import com.ericsson.component.aia.services.exteps.ioadapter.common.MessagingService;
import com.ericsson.component.aia.services.exteps.ioadapter.dfconsumer.DataCollector;

/**
 * A generic input adapter implementation
 *
 * @author eachsaj
 *
 */
public class GenericInputAdapter extends AbstractEventHandler implements InputAdapter, Runnable {

    /**
     * this is not used right now
     */
    private EpsStatisticsRegister statisticsRegister;

    // private Meter eventMeter;
    // message type
    private MessageServiceTypes uRI;

    // processor
    private DataCollector createDataCollector;

    private EventHandlerContext eventHandlerContext;

    private ProcessorThreadFactory threadfactory;

    @Override
    public boolean understandsURI(final String uri) {
        uRI = MessageServiceTypes.getURI(uri);
        return true;
    }

    @Override
    public void onEvent(final Object inputEvent) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void destroy() {
        if (createDataCollector != null) {
            createDataCollector.stop();
        }
        if (threadfactory != null && !threadfactory.getGroup().isDestroyed()) {
            threadfactory.getGroup().destroy();
        }
    }

    @Override
    protected void doInit() {
        log.info("Intializing generic io adapter.");
        eventHandlerContext = getContext();
        final Properties props = getAllProperties();
        final String type = props.getProperty(IO_CONSTANTS.EPS_FORMAT_DATA);
        checkArgument(type != null,
                "EPS event format property cannot be null. Please define flow input adapter property " + IO_CONSTANTS.EPS_FORMAT_DATA);
        createDataCollector = MessagingService.getInstance().getConsumerService(type);
        threadfactory = new ProcessorThreadFactory("GenericInputAdapter-ProcessFactory", 1024, 7);
        threadfactory.newThread(this).start();
    }

    /**
     * Creates connection to Kafka
     *
     * @param eventHandlerContext
     *            instance of {@link EventHandlerContext}
     * @param props
     *            connection properties
     */
    protected void createConnection(final EventHandlerContext eventHandlerContext, final Properties props) {
        createDataCollector.create(uRI, eventHandlerContext, props);
    }

    /**
     *
     * Method collects all properties and converts into a {@link Properties} object and returns
     */
    protected Properties getAllProperties() {
        final Map<String, Object> allProperties = this.getConfiguration().getAllProperties();
        final Properties props = new Properties();
        props.putAll(allProperties);
        return props;
    }

    /**
     * returns the current context
     *
     * @return
     */
    protected EventHandlerContext getContext() {
        return this.getEventHandlerContext();
    }

    /**
     * Initialise statistics.
     */
    /*
     * protected void initialiseStatistics() { statisticsRegister = (EpsStatisticsRegister) getEventHandlerContext().getContextualData(
     * EpsEngineConstants.STATISTICS_REGISTER_CONTEXTUAL_DATA_NAME); if (statisticsRegister == null) {
     * log.error("statisticsRegister should not be null"); } else { if (statisticsRegister.isStatisticsOn()) { eventMeter =
     * statisticsRegister.createMeter(topicName + "_eventReceived", this); } } }
     */

    /**
     * @return true if statistics is enabled
     */
    protected boolean isStatisticsOn() {
        return (statisticsRegister != null) && statisticsRegister.isStatisticsOn();
    }

    /**
     * @return the uRI
     */
    public MessageServiceTypes getuRI() {
        return uRI;
    }

    /**
     * @return the createDataCollector
     */
    public DataCollector getDataCollector() {
        return createDataCollector;
    }

    @Override
    public void run() {
        createConnection(eventHandlerContext, getAllProperties());

    }

}
