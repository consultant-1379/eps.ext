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

import java.util.HashMap;
import java.util.Map;

import com.ericsson.component.aia.itpf.common.config.Configuration;

/**
 * This class creates EPS configuration to be used in {@link ConfigurationUtilsTest}.
 * 
 * @since 0.0.1-SNAPSHOT
 */
public class ConfigurationUtilsConfigTest implements Configuration {
	
	protected static final String NUMBER_VALUE_PROP_NAME = "numberValue";
	protected static final String NUMBER_VALUE_NEGATIVE_PROP_NAME = "numberValueNegative";
	protected static final String NUMBER_VALUE_ZERO_PROP_NAME = "numberValueZero";
	protected static final String NUMBER_VALUE_LONG_PROP_NAME = "numberValueLong";
	protected static final String NUMBER_VALUE_STRING_PROP_NAME = "strValue";
	protected static final String NUMBER_VALUE_DECIMAL_PROP_NAME = "decValue";
	
	private int numberValue;
	private int numberValueNegative;
	private int numberValueZero;
	private int numberValueLong;
	private String strValue;
	private double decValue;
	
	private Map<String, Object> configMap;
	
	public ConfigurationUtilsConfigTest(final int numberValue, final int numberValueNegative, final int numberValueZero,
			final int numberValueLong, final String strValue, final double decValue) {
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
		
		populateConfigMap();
	}
	
	/**
	 * @param numberValue
	 * @param numberValueNegative
	 * @param numberValueZero
	 * @param numberValueLong
	 * @param strValue
	 * @param decValue
	 */
	private void populateConfigMap() {
		configMap = new HashMap<String, Object>();
		
		configMap.put(NUMBER_VALUE_PROP_NAME, numberValue);
		configMap.put(NUMBER_VALUE_NEGATIVE_PROP_NAME, numberValueNegative);
		configMap.put(NUMBER_VALUE_ZERO_PROP_NAME, numberValueZero);
		configMap.put(NUMBER_VALUE_LONG_PROP_NAME, numberValueLong);
		configMap.put(NUMBER_VALUE_STRING_PROP_NAME, strValue);
		configMap.put(NUMBER_VALUE_DECIMAL_PROP_NAME, decValue);
	}
	
	@Override
	public Integer getIntProperty(final String propertyName) {
		return null;
	}
	
	@Override
	public String getStringProperty(final String paramName) {
		for (final String configParam : configMap.keySet()) {
			if (configParam == paramName) {
				return configMap.get(configParam).toString();
			}
		}
		
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
	
}
