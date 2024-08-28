package com.ericsson.component.aia.services.exteps.policy;

import org.apache.kafka.clients.producer.ProducerRecord;

import com.ericsson.component.aia.services.exteps.policy.EventListener;
import com.ericsson.component.aia.services.exteps.policy.PolicyBasedEventConsumer;
import com.ericsson.component.aia.services.exteps.policy.PolicyBasedEventSender;
import com.ericsson.component.aia.services.exteps.policy.impl.PolicyBasedEventConsumerFactory;
import com.ericsson.component.aia.services.exteps.policy.impl.PolicyBasedEventSenderFactory;
import com.ericsson.component.aia.services.exteps.policy.impl.ProducerEvent;

import junit.framework.TestCase;

/**
 * Unit test for simple App.
 */
public class PolicyHandlerTest  extends TestCase
{ 
    
    public void testProducer()
    {
        PolicyBasedEventSender<String, String> eventSender = new PolicyBasedEventSenderFactory<String,String>("/usr/local").eventSender("LTE_CELL_TRACE_POLICY");
        ProducerEvent<String,String> event = new ProducerEvent<String,String>("MOBILITY");
        event.setKey("x");
        event.setValue("x");
        eventSender.sendEvent(event);
    }
    
    public void testConsumer()
    {
    	PolicyBasedEventConsumer<String, String> eventConsumer = new PolicyBasedEventConsumerFactory<String,String>("127.0.0.1:2181").eventConsumer("LTE_CELL_TRACE_POLICY");
    	eventConsumer.registerEventListener(new EventListener<String, String>() {
			
			@Override
			public void onEvent(ProducerRecord<String, String> event) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onEvent(ProducerEvent<String, String> event) {
				// TODO Auto-generated method stub
				
			}
		});
        
    }
}
