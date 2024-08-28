package com.ericsson.component.aia.services.exteps.ioadapter.kafka;

import java.util.Map;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import com.codahale.metrics.Meter;
import com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler;
import com.ericsson.component.aia.itpf.common.io.OutputAdapter;
import com.ericsson.component.aia.services.eps.EpsEngineConstants;
import com.ericsson.component.aia.services.eps.statistics.EpsStatisticsRegister;

/**
 * Output adapter to Kafka
 * 
 * @author eborziv
 * 
 */
public class KafkaOutputAdapter extends AbstractEventHandler implements OutputAdapter {

    private static final String RECORD_CREATOR_CLASS_NAME_PROPERTY = "recordCreatorClassName";

    private KafkaProducerRecordCreator recordCreator;

    private KafkaProducer kafkaProducer;

    private EpsStatisticsRegister statisticsRegister;

    private Meter eventMeter;

    @Override
    public boolean understandsURI(final String uri) {
        return uri != null && uri.equalsIgnoreCase(KafkaIOConstants.KAFKA_URI_PREFIX);
    }

    @Override
    public void onEvent(final Object inputEvent) {
        final ProducerRecord record = this.recordCreator.getProducerRecordForEvent(inputEvent);
        // we allow creators to skip some events and avoid sending them downstream
        if (record != null) {
            kafkaProducer.send(record);
            if (isStatisticsOn()) {
                eventMeter.mark();
            }
        } else {
            log.info("{} returned null for {}. Will not send anything downstream!", this.recordCreator, inputEvent);
        }
    }

    @Override
    public void destroy() {
        if (kafkaProducer != null) {
            kafkaProducer.close();
        }
    }

    @Override
    protected void doInit() {
        final Map<String, Object> allProperties = this.getConfiguration().getAllProperties();
        kafkaProducer = new KafkaProducer(allProperties);
        log.debug("Created KafkaProducer for properties {}", allProperties);
        final String recordCreatorClassName = this.getConfiguration().getStringProperty(RECORD_CREATOR_CLASS_NAME_PROPERTY);
        if (recordCreatorClassName == null || recordCreatorClassName.trim().isEmpty()) {
            throw new IllegalArgumentException("Was not able to find value for configuration property " + RECORD_CREATOR_CLASS_NAME_PROPERTY);
        }
        try {
            final Class clazz = Class.forName(recordCreatorClassName);
            log.debug("Successfully found class " + recordCreatorClassName);
            final Object recordCreatorInstance = clazz.newInstance();
            if (!(recordCreatorInstance instanceof KafkaProducerRecordCreator)) {
                throw new IllegalArgumentException("Class " + recordCreatorClassName + " does not implement interface "
                        + KafkaProducerRecordCreator.class);
            }
            recordCreator = (KafkaProducerRecordCreator) recordCreatorInstance;
            log.debug("Successfully initialized record creator instance {}", recordCreator);
            initialiseStatistics();
        } catch (final ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
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
                eventMeter = statisticsRegister.createMeter(recordCreator.getClass().getSimpleName() + "_eventSent", this);
            }
        }
    }

    /**
     * @return true if statistics is enabled
     */
    protected boolean isStatisticsOn() {
        return (statisticsRegister != null) && statisticsRegister.isStatisticsOn();
    }
}
