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
package com.ericsson.component.aia.services.exteps.ioadapter.output;

import java.util.Map;
import java.util.Properties;

import com.ericsson.component.aia.common.transport.service.MessageServiceTypes;
import com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.component.aia.itpf.common.io.OutputAdapter;
import com.ericsson.component.aia.services.exteps.ioadapter.builder.IO_CONSTANTS;
import com.ericsson.component.aia.services.exteps.ioadapter.common.MessagingService;
import com.ericsson.component.aia.services.exteps.ioadapter.dfproducer.DataProducer;
import com.google.common.base.Preconditions;

/**
 * @author eachsaj
 * May 3, 2016
 */
public class GenericOutputAdapter  extends AbstractEventHandler implements OutputAdapter {

    /**
     * {@link MessageServiceTypes} reference
     */
    private MessageServiceTypes uRI;
    /**
     * A data handler for producer
     */
    private DataProducer dataProducer;

    /* (non-Javadoc)
     * @see com.ericsson.oss.itpf.common.io.Adapter#understandsURI(java.lang.String)
     */
    @Override
    public boolean understandsURI(String uri) {
        uRI = MessageServiceTypes.getURI(uri); //otherwise exception
        return true;
    }

    /* (non-Javadoc)
     * @see com.ericsson.oss.itpf.common.event.handler.EventInputHandler#onEvent(java.lang.Object)
     */
    @Override
    public void onEvent(Object inputEvent) {
        dataProducer.publish(inputEvent);
    }

    /* (non-Javadoc)
     * @see com.ericsson.oss.itpf.common.event.handler.AbstractEventHandler#doInit()
     */
    @Override
    protected void doInit() {
        log.info("Intializing generic io adapter.");
        final EventHandlerContext eventHandlerContext = getContext();
        final Properties props = getAllProperties();
        final String type = props.getProperty(IO_CONSTANTS.EPS_FORMAT_DATA);
        Preconditions.checkArgument(type != null, "EPS event format property cannot be null. Please define flow input adapter property " + IO_CONSTANTS.EPS_FORMAT_DATA);
        dataProducer = MessagingService.getInstance().getPublisherService(type);
        connect(eventHandlerContext, props);

    }

    /**
     * A method that connects to Kafka broker
     * @param eventHandlerContext instance of {@link EventHandlerContext}
     * @param props connection properties
     */
    protected void connect(EventHandlerContext eventHandlerContext, Properties props) {
        dataProducer.create(uRI, eventHandlerContext, props);
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
     * @return
     */
    protected EventHandlerContext getContext() {
        return this.getEventHandlerContext();
    }
    /**
     * @return the uRI
     */
    public MessageServiceTypes getuRI() {
        return uRI;
    }

    /**
     * @return the dataProducer
     */
    public DataProducer getDataProducer() {
        return dataProducer;
    }

}
