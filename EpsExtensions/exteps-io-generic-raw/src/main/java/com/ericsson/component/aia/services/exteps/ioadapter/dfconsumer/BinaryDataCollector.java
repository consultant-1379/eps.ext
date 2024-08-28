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
package com.ericsson.component.aia.services.exteps.ioadapter.dfconsumer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.util.Properties;

import com.ericsson.component.aia.common.transport.service.MessageServiceTypes;
import com.ericsson.component.aia.common.transport.service.Subscriber;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.component.aia.services.exteps.ioadapter.builder.IO_CONSTANTS;
import com.ericsson.component.aia.services.exteps.ioadapter.builder.ServiceFactory;
import com.google.auto.service.AutoService;

/**
 * This class handling the binary data consumer requests
 * @author eachsaj
 * March 3, 2016
 */
@AutoService(DataCollector.class)
public class BinaryDataCollector implements DataCollector {

    private EPSMessageListener<byte[]> listener;

    private Subscriber<String, byte[]> createSubscriber;
    /* (non-Javadoc)
     * @see com.ericsson.component.aia.services.exteps.ioadapter.dfHandler.DataCollector#create(com.ericsson.oss.itpf.common.event.handler.EventHandlerContext, java.util.Properties)
     */
    @Override
    public void  create(MessageServiceTypes uri, EventHandlerContext eventHandlerContext, Properties props) {
        checkArgument(eventHandlerContext != null, "EventHandlerContext cannot be null.");
        checkArgument(props != null, "Properties cannot be null.");
        checkState(listener == null, "listener is already initialized");
        createListener(eventHandlerContext, props);
        connectConsumer(uri, props);
    }


    /**
     * Connect to the consumer type
     * @param uri
     * @param props
     */
    private void connectConsumer(MessageServiceTypes uri, Properties props) {
        createSubscriber = ServiceFactory.createSubscriber(uri, props, listener);
        createSubscriber.start();
    }

    /**
     * @param eventHandlerContext
     * @param props
     */
    private void createListener(EventHandlerContext eventHandlerContext, Properties props) {
        listener = new EPSMessageListener<>();
        listener.init(props, eventHandlerContext);
    }

    /* (non-Javadoc)
     * @see com.ericsson.component.aia.services.exteps.ioadapter.dfHandler.DataCollector#stop()
     */
    @Override
    public void stop() {
        checkState(listener != null, "listener is not active");
        listener.stop();
    }

    /* (non-Javadoc)
     * @see com.ericsson.component.aia.services.exteps.ioadapter.dfHandler.DataCollector#getListener()
     */
    @SuppressWarnings("unchecked")
    public EPSMessageListener<byte[]> getListener() {
        return listener;
    }


    /* (non-Javadoc)
     * @see com.ericsson.component.aia.services.exteps.ioadapter.dfconsumer.DataCollector#identify(java.lang.String)
     */
    @Override
    public boolean identify(String arg0) {
        return arg0.equalsIgnoreCase(IO_CONSTANTS.FORMAT_RAW);
    }

}
