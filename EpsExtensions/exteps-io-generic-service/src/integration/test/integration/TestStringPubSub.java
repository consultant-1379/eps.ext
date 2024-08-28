package test.integration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.ericsson.component.aia.itpf.common.config.Configuration;
import com.ericsson.component.aia.itpf.common.event.ControlEvent;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.component.aia.itpf.common.event.handler.EventSubscriber;
import com.ericsson.component.aia.services.exteps.ioadapter.common.IO_CONSTANTS;
import com.ericsson.component.aia.services.exteps.ioadapter.input.GenericInputAdapter;

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

/**
 * @author eachsaj
 */
public class TestStringPubSub {
	
	/**
	 * @author eachsaj
	 */
	private static final class SubscriberTestHandler implements EventHandlerContext {
		@Override
		public void sendControlEvent(ControlEvent controlEvent) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public Collection<EventSubscriber> getEventSubscribers() {
			Collection <EventSubscriber>  listeners = new ArrayList<>();
			listeners.add(new EventSubscriberTest());
			return listeners;
		}
		
		@Override
		public Configuration getEventHandlerConfiguration() {
			// TODO Auto-generated method stub
			return new Configuration() {
				Properties props = new Properties();
				{
			     props.put("bootstrap.servers", "localhost:9092");
			     props.put("group.id", "testcommon");
			     props.put("enable.auto.commit", "true");
			     props.put("auto.commit.interval.ms", "1000");
			     props.put("session.timeout.ms", "30000");
			     props.put(IO_CONSTANTS.EPS_FORMAT_DATA, IO_CONSTANTS.FORMAT_RAW);
				 props.put("partitioner.class", "integration.StringPartitioner");
			     props.put(IO_CONSTANTS.KAFKA_DESERIALIZER_CLASS, "org.apache.kafka.common.serialization.StringDeserializer");
			     props.put(IO_CONSTANTS.KAFKA_VALUE_DE_SERIALIZER_CLASS, " org.apache.kafka.common.serialization.ByteArrayDeserializer");
			     props.put(IO_CONSTANTS.EPS_FORMAT_DATA, IO_CONSTANTS.FORMAT_STRING);
			     props.put(IO_CONSTANTS.INPUT_THREAD_POOL_SIZE_PARAM, "3");
			     props.put(IO_CONSTANTS.KAFKA_TOPIC_NAME, "raw");
			     
			    
				}
				@Override
				public String getStringProperty(String propertyName) {
					// TODO Auto-generated method stub
					return props.getProperty(propertyName);
				}
				
				@Override
				public Integer getIntProperty(String propertyName) {
					// TODO Auto-generated method stub
					return Integer.valueOf(props.getProperty(propertyName));
				}
				
				@Override
				public Boolean getBooleanProperty(String propertyName) {
					return Boolean.valueOf(props.getProperty(propertyName));
				}
				
				@Override
				public Map<String, Object> getAllProperties() {
					Map<String, Object> map = new HashMap<>();
					for (String key : props.stringPropertyNames()) {
					    map.put(key, props.getProperty(key));
					}
					return map;
				}
			};
		}
		
		@Override
		public Object getContextualData(String name) {
			// TODO Auto-generated method stub
			return null;
		}
	}

	public static void main(String [] ar){
		GenericInputAdapter adapter = new GenericInputAdapter();
		adapter.understandsURI("Kafka:/");
		adapter.init(new SubscriberTestHandler());
	}
	
}

class EventSubscriberTest implements EventSubscriber{

	@Override
	public String getIdentifier() {
		return "test";
	}
static int i=0;
	@Override
	public void sendEvent(Object event) {
		Collection<byte[]> records =(Collection<byte[]>)event ;
		
		for (byte[] string : records) {
			System.out.println("count"+i+""+new String(string));
			i++;
		}
		
		
	}
	
}
