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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ericsson.component.aia.itpf.common.config.Configuration;
import com.ericsson.component.aia.itpf.common.event.ControlEvent;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.component.aia.itpf.common.event.handler.EventSubscriber;
import com.ericsson.component.aia.services.exteps.ioadapter.builder.IO_CONSTANTS;
import com.ericsson.component.aia.services.exteps.ioadapter.output.GenericOutputAdapter;

/**
 * @author eachsaj
 * May 8, 2016
 */
public class GenericOutputAdapterTest {
	
	class TestGenericOutputAdapter extends GenericOutputAdapter{
		protected void connect(EventHandlerContext eventHandlerContext, Properties props) {
		}
	}
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}
	
	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}
	
	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}
	
	/**
	 * Test method for {@link com.ericsson.component.aia.services.exteps.ioadapter.output.GenericOutputAdapter#doInit()}.
	 */
	/**
	 * 
	 */
	@Test(expected= IllegalArgumentException.class)
	public void testDoInitFailed() {
		GenericOutputAdapter instance = new TestGenericOutputAdapter();
		instance.understandsURI("kafka:/");
		instance.init(new LocalTestHandlerWrong());
		Assert.assertNotNull(instance.getDataProducer());
	}
	@Test
	public void testDoInit() {
		GenericOutputAdapter instance = new TestGenericOutputAdapter();
		instance.understandsURI("kafka:/");
		instance.init(new LocalTestHandler());
		Assert.assertNotNull(instance.getDataProducer());
	}
	
}
/**
 * @author eachsaj
 */
  final class LocalTestHandler implements EventHandlerContext {
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
		     props.put(IO_CONSTANTS.EPS_FORMAT_DATA, IO_CONSTANTS.FORMAT_STRING);
		     props.put(IO_CONSTANTS.OUTPUT_THREAD_POOL_SIZE_PARAM, "3");
		     props.put(IO_CONSTANTS.KAFKA_TOPIC_NAME, "ctumtest");
		     
		 
		    
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
  final class LocalTestHandlerWrong implements EventHandlerContext {
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
			     props.put(IO_CONSTANTS.EPS_FORMAT_DATA, "wrong");
			     props.put(IO_CONSTANTS.OUTPUT_THREAD_POOL_SIZE_PARAM, "3");
			     props.put(IO_CONSTANTS.KAFKA_TOPIC_NAME, "ctumtest");
			     
			 
			    
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
