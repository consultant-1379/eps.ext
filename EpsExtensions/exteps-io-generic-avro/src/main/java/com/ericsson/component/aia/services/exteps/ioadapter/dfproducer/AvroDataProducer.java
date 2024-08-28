/**
 *
 * (C) Copyright LM Ericsson System Expertise AT/LMI, 2016
 *
 * The copyright to the computer program(s) herein is the property of Ericsson  System Expertise EEI, Sweden.
 * The program(s) may be used and/or copied only with the written permission from Ericsson System Expertise
 * AT/LMI or in  * accordance with the terms and conditions stipulated in the agreement/contract under which
 * the program(s) have been supplied.
 *
 */
package com.ericsson.component.aia.services.exteps.ioadapter.dfproducer;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Properties;

import org.apache.avro.generic.GenericRecord;

import com.ericsson.component.aia.common.transport.service.MessageServiceTypes;
import com.ericsson.component.aia.common.transport.service.Publisher;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.component.aia.services.exteps.ioadapter.builder.IO_CONSTANTS;
import com.ericsson.component.aia.services.exteps.ioadapter.builder.ServiceFactory;
import com.ericsson.component.aia.services.exteps.ioadapter.dfconsumer.DataCollector;
import com.google.auto.service.AutoService;

/**
 * Class handling the Avro data publishing
 * @author eachsaj
 * May 5, 2016
 */
@AutoService(DataCollector.class)
public class AvroDataProducer implements DataProducer {

    private Publisher<String, GenericRecord> createProducer;
    private String topic;

    /* (non-Javadoc)
     * @see com.ericsson.component.aia.services.exteps.ioadapter.dfproducer.DataProducer#create(com.ericsson.component.aia.common.transport.service.MessageServiceTypes, com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext, java.util.Properties)
     */
    @Override
    public void create(MessageServiceTypes uri, EventHandlerContext eventHandlerContext, Properties props) {
        checkArgument(eventHandlerContext != null, "EventHandlerContext cannot be null.");
        checkArgument(props != null, "Properties cannot be null.");
        connectPublisher(uri, props);
        topic = props.getProperty(IO_CONSTANTS.KAFKA_TOPIC_NAME);

    }
    /**
     * Connect to the producer type
     * @param uri is the   chosen technology types
     * @param props are the connection properties
     */
    private void connectPublisher(MessageServiceTypes uri, Properties props) {
        createProducer = ServiceFactory.<String, GenericRecord> createPublisher(uri, props);

    }

    /* (non-Javadoc)
     * @see com.ericsson.component.aia.services.exteps.ioadapter.dfproducer.DataProducer#stop()
     */
    @Override
    public void stop() {
        if (createProducer.isconnected()) {
            createProducer.close();
        }
    }
    /**
     * A method to get Producer instance
     * @return the instance of {@link Publisher}
     */
    @SuppressWarnings("unchecked")
    @Override
    public  Publisher<String, GenericRecord> getProducer() {
        return createProducer;
    }
    /* (non-Javadoc)
     * @see com.ericsson.component.aia.services.exteps.ioadapter.dfproducer.DataProducer#publish(java.lang.Object)
     */
    @Override
    public <K, V> void publish(Object object) {
        final GenericRecord data = (GenericRecord) object;
        createProducer.sendMessage(topic, data);

    }


    /* (non-Javadoc)
     * @see com.ericsson.component.aia.services.exteps.ioadapter.dfconsumer.DataCollector#identify(java.lang.String)
     */
    @Override
    public boolean identify(String arg0) {
        return arg0.equalsIgnoreCase(IO_CONSTANTS.FORMAT_AVRO);
    }

}
