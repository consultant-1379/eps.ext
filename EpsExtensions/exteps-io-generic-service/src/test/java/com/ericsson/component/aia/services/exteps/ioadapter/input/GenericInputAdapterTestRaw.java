/**
 * 
 * (C) Copyright LM Ericsson System Expertise AT/LMI, 2016
 *
 * The copyright to the computer program(s) herein is the property of Ericsson System Expertise EEI, Sweden. The
 * program(s) may be used and/or copied only with the written permission from Ericsson System Expertise AT/LMI or in *
 * accordance with the terms and conditions stipulated in the agreement/contract under which the program(s) have been
 * supplied.
 *
 */
package com.ericsson.component.aia.services.exteps.ioadapter.input;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ericsson.component.aia.common.transport.service.MessageServiceTypes;
import com.ericsson.component.aia.itpf.common.config.Configuration;
import com.ericsson.component.aia.itpf.common.event.ControlEvent;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.component.aia.itpf.common.event.handler.EventSubscriber;
import com.ericsson.component.aia.services.exteps.ioadapter.dfconsumer.BinaryDataCollector;
import com.ericsson.component.aia.services.exteps.ioadapter.dfconsumer.StringDataCollector;

/**
 * @author eachsaj May 7, 2016
 */
public class GenericInputAdapterTestRaw {
	GenericInputAdapter inputA = null;
	private static Properties properties = new Properties();
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		properties.put("uri", "kafka:/");
		properties.put("group.id", "raw_consumer_group");
		properties.put("bootstrap.servers", "localhost:9092");
		properties.put("enable.auto.commit", "true");
		properties.put("auto.commit.interval.ms", "1000");
		properties.put("session.timeout.ms", "30000");
		properties.put("eps.format.data", "tcp");
		properties.put("kafka.Deserializer.class", "org.apache.kafka.common.serialization.StringDeserializer");
		properties.put("kafka.serializer.class", " org.apache.kafka.common.serialization.StringDeserializer");
		properties.put("eps.input.thread.pool.size", "1");
		properties.put("eps.kafka.topicName", "ctr-data");
		properties.put("rebalance.max.retries", "3000");
	}
	
	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}
	
	class TestGenericInputDater extends GenericInputAdapter {
		protected void createConnection(EventHandlerContext eventHandlerContext, Properties props) {
		}
	}
	
	@Before
	public void setup() {
		inputA = new TestGenericInputDater();
		
	}
	
	/**
	 * Test method for {@link com.ericsson.component.aia.services.exteps.ioadapter.input.GenericInputAdapter#doInit()}.
	 */
	@Test
	public void testDoInit() {
		inputA.understandsURI("kafka:/");
		inputA.init(new SubscriberTestHandlerRaw());
		assertNotNull(inputA.getDataCollector());
		assertTrue(inputA.getDataCollector() instanceof BinaryDataCollector);
	}
	/**
	 * Test method for {@link com.ericsson.component.aia.services.exteps.ioadapter.input.GenericInputAdapter#doInit()}.
	 */
	@Test
	public void testDoInitFail() {
		inputA.understandsURI("kafka:/");
		inputA.init(new SubscriberTestHandlerRaw());
		assertNotNull(inputA.getDataCollector());
		assertFalse(inputA.getDataCollector() instanceof StringDataCollector);
	}
	@Test(expected=IllegalArgumentException.class)
	public void testDoInitFailAnother() {
		inputA.understandsURI("kafka:/");
		inputA.init(new SubscriberTestHandlerRawFail());
	}
	/**
	 * Test method for
	 * {@link com.ericsson.component.aia.services.exteps.ioadapter.input.GenericInputAdapter#understandsURI(java.lang.String)}.
	 */
	@Test
	public void testUnderstandsURI() {
		assertTrue(inputA.understandsURI("kafka:/"));
	}
	
	/**
	 * Test method for {@link com.ericsson.component.aia.services.exteps.ioadapter.input.GenericInputAdapter#getuRI()}.
	 */
	@Test
	public void testGetuRI() {
		inputA.understandsURI("kafka:/");
		assertEquals(MessageServiceTypes.KAFKA, inputA.getuRI());
	}
	
}

/**
 * @author eachsaj
 */
final class SubscriberTestHandlerRaw implements EventHandlerContext {
	@Override
	public void sendControlEvent(ControlEvent controlEvent) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Collection<EventSubscriber> getEventSubscribers() {
		Collection<EventSubscriber> listeners = new ArrayList<>();
		listeners.add(new EventSubscriberTestRaw());
		return listeners;
	}
	
	@Override
	public Configuration getEventHandlerConfiguration() {
		// TODO Auto-generated method stub
		return new Configuration() {
			Properties properties = new Properties();
			{
				properties.put("uri", "kafka:/");
				properties.put("group.id", "raw_consumer_group");
				properties.put("bootstrap.servers", "localhost:9092");
				properties.put("enable.auto.commit", "true");
				properties.put("auto.commit.interval.ms", "1000");
				properties.put("session.timeout.ms", "30000");
				properties.put("eps.format.data", "tcp");
				properties.put("kafka.Deserializer.class", "org.apache.kafka.common.serialization.StringDeserializer");
				properties.put("kafka.serializer.class", " org.apache.kafka.common.serialization.StringDeserializer");
				properties.put("eps.input.thread.pool.size", "1");
				properties.put("eps.kafka.topicName", "ctr-data");
				properties.put("rebalance.max.retries", "3000");
				
			}
			
			@Override
			public String getStringProperty(String propertyName) {
				// TODO Auto-generated method stub
				return properties.getProperty(propertyName);
			}
			
			@Override
			public Integer getIntProperty(String propertyName) {
				// TODO Auto-generated method stub
				return Integer.valueOf(properties.getProperty(propertyName));
			}
			
			@Override
			public Boolean getBooleanProperty(String propertyName) {
				return Boolean.valueOf(properties.getProperty(propertyName));
			}
			
			@Override
			public Map<String, Object> getAllProperties() {
				Map<String, Object> map = new HashMap<>();
				for (String key : properties.stringPropertyNames()) {
					map.put(key, properties.getProperty(key));
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

/**
 * @author eachsaj
 */
final class SubscriberTestHandlerRawFail implements EventHandlerContext {
	@Override
	public void sendControlEvent(ControlEvent controlEvent) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Collection<EventSubscriber> getEventSubscribers() {
		Collection<EventSubscriber> listeners = new ArrayList<>();
		listeners.add(new EventSubscriberTestRaw());
		return listeners;
	}
	
	@Override
	public Configuration getEventHandlerConfiguration() {
		// TODO Auto-generated method stub
		return new Configuration() {
			Properties properties = new Properties();
			{
				properties.put("uri", "kafka:/");
				properties.put("group.id", "raw_consumer_group");
				properties.put("bootstrap.servers", "localhost:9092");
				properties.put("enable.auto.commit", "true");
				properties.put("auto.commit.interval.ms", "1000");
				properties.put("session.timeout.ms", "30000");
				properties.put("kafka.Deserializer.class", "org.apache.kafka.common.serialization.StringDeserializer");
				properties.put("kafka.serializer.class", " org.apache.kafka.common.serialization.StringDeserializer");
				properties.put("eps.input.thread.pool.size", "1");
				properties.put("eps.kafka.topicName", "ctr-data");
				properties.put("rebalance.max.retries", "3000");
				
			}
			
			@Override
			public String getStringProperty(String propertyName) {
				// TODO Auto-generated method stub
				return properties.getProperty(propertyName);
			}
			
			@Override
			public Integer getIntProperty(String propertyName) {
				// TODO Auto-generated method stub
				return Integer.valueOf(properties.getProperty(propertyName));
			}
			
			@Override
			public Boolean getBooleanProperty(String propertyName) {
				return Boolean.valueOf(properties.getProperty(propertyName));
			}
			
			@Override
			public Map<String, Object> getAllProperties() {
				Map<String, Object> map = new HashMap<>();
				for (String key : properties.stringPropertyNames()) {
					map.put(key, properties.getProperty(key));
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

class EventSubscriberTestRaw implements EventSubscriber {
	
	@Override
	public String getIdentifier() {
		return "test";
	}
	
	static int i = 0;
	
	@Override
	public void sendEvent(Object event) {
		Collection<String> records = (Collection<String>) event;
		
		for (String string : records) {
			System.out.println("count" + i + "" + (string));
			i++;
		}
		
	}
	
}