/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.exteps.ioadapter.file;

import java.util.Collection;
import java.util.Map;

import com.ericsson.component.aia.itpf.common.config.Configuration;
import com.ericsson.component.aia.itpf.common.event.ControlEvent;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.component.aia.itpf.common.event.handler.EventSubscriber;

/**
 * This class creates EPS event handler context to be used in {@link ConfigurationUtilsTest}.
 * 
 * @since 0.0.1-SNAPSHOT
 */
public class ConfigurationUtilsEventHandlerContextTest implements EventHandlerContext, Configuration {
	
	private int numberValue;
	private int numberValueNegative;
	private int numberValueZero;
	private int numberValueLong;
	private String strValue;
	private double decValue;
	
	public ConfigurationUtilsEventHandlerContextTest(final int numberValue, final int numberValueNegative,
			final int numberValueZero, final int numberValueLong, final String strValue, final double decValue) {
		setVariables(numberValue, numberValueNegative, numberValueZero, numberValueLong, strValue, decValue);
	}
	
	/**
	 * @param numberValue
	 * @param numberValueNegative
	 * @param numberValueZero
	 * @param numberValueLong
	 * @param strValue
	 * @param decValue
	 */
	private void setVariables(final int numberValue, final int numberValueNegative, final int numberValueZero,
			final int numberValueLong, final String strValue, final double decValue) {
		this.numberValue = numberValue;
		this.numberValueNegative = numberValueNegative;
		this.numberValueZero = numberValueZero;
		this.numberValueLong = numberValueLong;
		this.strValue = strValue;
		this.decValue = decValue;
	}
	
	@Override
	public Configuration getEventHandlerConfiguration() {
		return new ConfigurationUtilsConfigTest(numberValue, numberValueNegative, numberValueZero, numberValueLong,
				strValue, decValue);
	}
	
	@Override
	public Collection<EventSubscriber> getEventSubscribers() {
		return null;
	}
	
	@Override
	public void sendControlEvent(final ControlEvent controlEvent) {
		
	}
	
	@Override
	public Integer getIntProperty(final String propertyName) {
		return null;
	}
	
	@Override
	public String getStringProperty(final String propertyName) {
		return null;
	}
	
	@Override
	public Boolean getBooleanProperty(final String propertyName) {
		return null;
	}
	
	@Override
	public Map<String, Object> getAllProperties() {
		return null;
	}
	
	@Override
	public Object getContextualData(final String name) {
		return null;
	}
	
}
