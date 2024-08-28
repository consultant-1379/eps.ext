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

import static com.ericsson.component.aia.services.exteps.ioadapter.file.ConfigurationUtils.*;
import static com.ericsson.component.aia.services.exteps.ioadapter.file.utils.FileInputAdapterConfigParams.*;
import static org.junit.Assert.*;

import org.junit.*;
import org.mockito.Mockito;

import com.ericsson.component.aia.services.exteps.ioadapter.file.ConfigurationUtils;
import com.ericsson.component.aia.services.exteps.ioadapter.file.FileCollectionInputAdapter;
import com.ericsson.component.aia.services.exteps.ioadapter.file.utils.StubbedConfiguration;
import com.ericsson.component.aia.itpf.common.config.Configuration;

/**
 * This class is a test class for {@link ConfigurationUtils}
 * 
 * @since 0.0.1-SNAPSHOT
 */
public class ConfigurationUtilsTest {
	
	FileCollectionInputAdapter fileCollectionInputAdapterMock;
	
	private Configuration config;
	
	private int numberValue;
	
	private int numberValueNegative;
	
	private int numberValueZero;
	
	private int numberValueLong;
	
	private String strValue;
	
	private double decValue;
	
	private static final String VALID_XML_FILE_REGEX = ".*_SubNetwork.*_MeContext.*.xml$";
	
	@Before
	public void setup() {
		fileCollectionInputAdapterMock = Mockito.mock(FileCollectionInputAdapter.class);
		
		numberValue = 5;
		numberValueNegative = -5;
		numberValueZero = 0;
		numberValueLong = 100000000;
		strValue = "abc";
		decValue = 1.5;
		
		final ConfigurationUtilsEventHandlerContextTest eventHandlerContext = new ConfigurationUtilsEventHandlerContextTest(
				numberValue, numberValueNegative, numberValueZero, numberValueLong, strValue, decValue);
		
		config = eventHandlerContext.getEventHandlerConfiguration();
	}
	
	@Test
	public void getIntegerConfigurationParamIfExists_configParamValueDoesNotExist_defaultValueReturned() {
		final String configParamName = "differentNumberValue";
		final int defaultValue = 8;
		final int result = ConfigurationUtils.getIntegerConfigurationParamIfExists(config, configParamName,
				defaultValue);
		
		Assert.assertEquals(defaultValue, result);
	}
	
	@Test
	public void getIntegerConfigurationParamIfExists_configParamValueExists_configValueReturned() {
		final String configParamName = ConfigurationUtilsConfigTest.NUMBER_VALUE_PROP_NAME;
		final int defaultValue = 3;
		final int result = ConfigurationUtils.getIntegerConfigurationParamIfExists(config, configParamName,
				defaultValue);
		
		Assert.assertEquals(numberValue, result);
	}
	
	@Test
	public void getIntegerConfigurationParamIfExists_configParamValueExistsIsNegative_defaultValueReturned() {
		final String configParamName = ConfigurationUtilsConfigTest.NUMBER_VALUE_NEGATIVE_PROP_NAME;
		final int defaultValue = 6;
		final int result = ConfigurationUtils.getIntegerConfigurationParamIfExists(config, configParamName,
				defaultValue);
		
		Assert.assertNotSame(numberValueNegative, result);
		Assert.assertEquals(defaultValue, result);
	}
	
	@Test
	public void getIntegerConfigurationParamIfExists_configParamValueExistsIsZero_defaultValueReturned() {
		final String configParamName = ConfigurationUtilsConfigTest.NUMBER_VALUE_ZERO_PROP_NAME;
		final int defaultValue = 10;
		final int result = ConfigurationUtils.getIntegerConfigurationParamIfExists(config, configParamName,
				defaultValue);
		
		Assert.assertNotSame(numberValueZero, result);
		Assert.assertEquals(defaultValue, result);
	}
	
	@Test
	public void getIntegerConfigurationParamIfExists_configParamValueExistsIsLargeValue_LargeValueReturned() {
		final String configParamName = ConfigurationUtilsConfigTest.NUMBER_VALUE_LONG_PROP_NAME;
		final int defaultValue = 10;
		final int result = ConfigurationUtils.getIntegerConfigurationParamIfExists(config, configParamName,
				defaultValue);
		
		Assert.assertNotSame(defaultValue, result);
		Assert.assertEquals(numberValueLong, result);
	}
	
	@Test
	public void getIntegerConfigurationParamIfExists_configParamValueExistsIsdecimal_defaultValueReturned() {
		final String configParamName = ConfigurationUtilsConfigTest.NUMBER_VALUE_DECIMAL_PROP_NAME;
		final int defaultValue = 10;
		final int result = ConfigurationUtils.getIntegerConfigurationParamIfExists(config, configParamName,
				defaultValue);
		
		Assert.assertNotSame(decValue, result);
		Assert.assertEquals(defaultValue, result);
	}
	
	@Test
	public void getIntegerConfigurationParamIfExists_configParamValueExistsIsString_defaultValueReturned() {
		final String configParamName = ConfigurationUtilsConfigTest.NUMBER_VALUE_STRING_PROP_NAME;
		final int defaultValue = 10;
		final int result = ConfigurationUtils.getIntegerConfigurationParamIfExists(config, configParamName,
				defaultValue);
		
		Assert.assertNotSame(strValue, result);
		Assert.assertEquals(defaultValue, result);
	}
	
	@Test
	public void getConfigurationParamIfExistsWithValidValues() {
		final StubbedConfiguration config = new StubbedConfiguration();
		final String value = VALID_XML_FILE_REGEX;
		config.setStringProperty(FILE_REGEX_PROP_NAME, value);
		assertEquals(value, getConfigurationParamIfExists(config, FILE_REGEX_PROP_NAME, ANY_FILE_REGEX));
	}
	
	@Test
	public void getConfigurationParamIfExistsWithInValidValues() {
		final StubbedConfiguration config = new StubbedConfiguration();
		final String value = "";
		config.setStringProperty(FILE_REGEX_PROP_NAME, value);
		assertEquals(ANY_FILE_REGEX, getConfigurationParamIfExists(config, FILE_REGEX_PROP_NAME, ANY_FILE_REGEX));
	}
	
	@Test
	public void getConfigurationParamIfExistsWithValuesNotDefined() {
		final StubbedConfiguration config = new StubbedConfiguration();
		assertEquals(ANY_FILE_REGEX, getConfigurationParamIfExists(config, FILE_REGEX_PROP_NAME, ANY_FILE_REGEX));
	}
	
	@Test
	public void getIntegerConfigurationParamIfExistsWithValuesNotDefined() {
		final StubbedConfiguration config = new StubbedConfiguration();
		config.setStringProperty(INTERVAL_VALUE_PROP_NAME, "abc");
		assertEquals(DEFAULT_INTERVAL_VALUE,
				getIntegerConfigurationParamIfExists(config, FILE_REGEX_PROP_NAME, DEFAULT_INTERVAL_VALUE));
	}
	
}
