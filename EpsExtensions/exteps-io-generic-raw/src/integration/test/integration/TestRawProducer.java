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
import com.ericsson.component.aia.services.exteps.ioadapter.output.GenericOutputAdapter;

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
public class TestRawProducer {
	
	/**
	 * @author eachsaj
	 */
	private static final class LocalTestHandler implements EventHandlerContext {
		@Override
		public void sendControlEvent(ControlEvent controlEvent) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public Collection<EventSubscriber> getEventSubscribers() {
			Collection <EventSubscriber>  listeners = new ArrayList<>();
			return listeners;
		}
		
		@Override
		public Configuration getEventHandlerConfiguration() {
			// TODO Auto-generated method stub
			return new Configuration() {
				Properties props = new Properties();
				{
					 props.put("bootstrap.servers", "localhost:9092");
					 props.put("acks", "all");
					 props.put("retries", 0);
					 props.put("batch.size", 16384);
					 props.put("linger.ms", 1);
					 props.put("buffer.memory", 33554432);
					 props.put("partitioner.class", "test.integration.StringPartitionerX");
					
					 props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
					 props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
			     props.put(IO_CONSTANTS.EPS_FORMAT_DATA, IO_CONSTANTS.FORMAT_RAW);
			     props.put(IO_CONSTANTS.KAFKA_DESERIALIZER_CLASS, "org.apache.kafka.common.serialization.StringDeserializer");
			     props.put(IO_CONSTANTS.KAFKA_VALUE_DE_SERIALIZER_CLASS, " org.apache.kafka.common.serialization.ByteArrayDeserializer");
			     props.put(IO_CONSTANTS.OUTPUT_THREAD_POOL_SIZE_PARAM, "3");
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
		GenericOutputAdapter adapter = new GenericOutputAdapter();
		adapter.understandsURI("Kafka:/");
		adapter.init(new LocalTestHandler());
		for (int i = 0; i < 10; i++) {
			adapter.onEvent(("DATA -"+i).getBytes());
		}
		
	}
	
}


	

