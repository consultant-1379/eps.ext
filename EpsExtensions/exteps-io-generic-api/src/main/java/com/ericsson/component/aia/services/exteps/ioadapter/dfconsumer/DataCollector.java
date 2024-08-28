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

import java.util.Properties;

import com.ericsson.component.aia.common.transport.service.MessageServiceTypes;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;

/**
 * An interface for handing the listener for different  data format.
 * @author eachsaj
 * March 3, 2016
 */
public interface DataCollector {
    /**
     * A method create instance of Data Collector instance. this method should initialize the colletor
     * and return the instance
     * @param eventHandlerContext instance of {@link EventHandlerContext}
     * @param props connection {@link Properties}
     */
    void create(MessageServiceTypes uri, EventHandlerContext eventHandlerContext, Properties props);

    /**
     * A method to stop the data collection
     */
    void stop();

    /**
     *
     * Returns the {@link EPSMessageListener} instance
     */
    <V>EPSMessageListener<V> getListener();

    /**
     * @param type String type possible, and possible values are tcp, string and avro
     * @return boolean value if the type acceptable otherwise returns  false
     */
    boolean identify(String type);



}