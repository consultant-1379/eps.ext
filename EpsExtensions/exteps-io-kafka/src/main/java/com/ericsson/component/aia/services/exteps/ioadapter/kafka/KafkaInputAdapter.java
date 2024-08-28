package com.ericsson.component.aia.services.exteps.ioadapter.kafka;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kafka.consumer.*;
import kafka.javaapi.consumer.ConsumerConnector;

import com.codahale.metrics.Meter;
import com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler;
import com.ericsson.component.aia.itpf.common.io.InputAdapter;
import com.ericsson.component.aia.services.eps.EpsEngineConstants;
import com.ericsson.component.aia.services.eps.statistics.EpsStatisticsRegister;

/**
 * Input adapter able to read from Kafka
 * 
 * @author eborziv
 * 
 */
public class KafkaInputAdapter extends AbstractEventHandler implements InputAdapter {

    private static final String THREAD_POOL_SIZE_PARAM = "threadPoolSize";
    private static final String TOPIC_NAME_PARAM = "topicName";

    private static final int THREAD_POOL_SIZE_DEFAULT = 5;

    private ExecutorService exec;

    private ConsumerConnector connector;

    private String topicName;

    private Integer threadNumber;

    private EpsStatisticsRegister statisticsRegister;

    private Meter eventMeter;

    @Override
    public boolean understandsURI(final String uri) {
        return uri != null && uri.equalsIgnoreCase(KafkaIOConstants.KAFKA_URI_PREFIX);
    }

    @Override
    public void onEvent(final Object inputEvent) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void destroy() {
        if (exec != null) {
            exec.shutdown();
        }
        if (connector != null) {
            connector.shutdown();
        }
    }

    /**
     * 
     * @param event
     *            the object to be send
     */
    void sendEvent(final Object event) {
        sendToAllSubscribers(event);
        if (isStatisticsOn()) {
            eventMeter.mark();
        }
    }

    @Override
    protected void doInit() {
        threadNumber = this.getConfiguration().getIntProperty(THREAD_POOL_SIZE_PARAM);
        if (threadNumber == null) {
            threadNumber = THREAD_POOL_SIZE_DEFAULT;
        } else if (threadNumber.intValue() <= 0) {
            throw new IllegalArgumentException("Value for " + THREAD_POOL_SIZE_PARAM + " must be positive integer!");
        }
        topicName = this.getConfiguration().getStringProperty(TOPIC_NAME_PARAM);
        if (topicName == null || topicName.trim().isEmpty()) {
            throw new IllegalArgumentException("Non-empty value for " + TOPIC_NAME_PARAM + " must be provided in configuration!");
        }
        log.debug("Will use {} threads for consuming messages from topic [{}]", threadNumber, topicName);
        final ConsumerConfig config = this.getConsumerConfig();
        connector = Consumer.createJavaConsumerConnector(config);
        this.createListeners();
        initialiseStatistics();
    }

    /**
     * Initialise statistics.
     */
    protected void initialiseStatistics() {
        statisticsRegister = (EpsStatisticsRegister) getEventHandlerContext().getContextualData(
                EpsEngineConstants.STATISTICS_REGISTER_CONTEXTUAL_DATA_NAME);
        if (statisticsRegister == null) {
            log.error("statisticsRegister should not be null");
        } else {
            if (statisticsRegister.isStatisticsOn()) {
                eventMeter = statisticsRegister.createMeter(topicName + "_eventReceived", this);
            }
        }
    }

    private void createListeners() {
        exec = Executors.newFixedThreadPool(threadNumber);
        final Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        topicCountMap.put(topicName, new Integer(threadNumber));
        final Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = connector.createMessageStreams(topicCountMap);
        final List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topicName);
        for (final KafkaStream<byte[], byte[]> stream : streams) {
            final KafkaConsumerListener listener = new KafkaConsumerListener(stream, this);
            exec.submit(listener);
        }
        log.debug("In total created {} listeners on topic [{}]", streams.size(), topicName);
    }

    private ConsumerConfig getConsumerConfig() {
        final Map<String, Object> allProperties = this.getConfiguration().getAllProperties();
        final Properties props = new Properties();
        props.putAll(allProperties);
        return new ConsumerConfig(props);
    }

    /**
     * @return true if statistics is enabled
     */
    protected boolean isStatisticsOn() {
        return (statisticsRegister != null) && statisticsRegister.isStatisticsOn();
    }

}
