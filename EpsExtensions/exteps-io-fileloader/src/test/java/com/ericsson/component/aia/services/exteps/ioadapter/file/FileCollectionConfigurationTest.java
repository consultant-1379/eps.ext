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

import static com.ericsson.component.aia.services.exteps.ioadapter.file.utils.FileInputAdapterConfigParams.*;

import java.util.HashMap;
import java.util.Map;

import com.ericsson.component.aia.itpf.common.config.Configuration;

/**
 * This class creates EPS configuration to be used in {@link FileCollectionInputAdapterTest}.
 * 
 * @since 0.0.1-SNAPSHOT
 */
public class FileCollectionConfigurationTest implements Configuration {
	
	private String directoryPath;
	private String directorys;
	private int initialDelay;
	private int intervalValue;
	
	private Map<String, Object> configMap;
	
	public FileCollectionConfigurationTest(final String directoryPath, final String directorys, final int initialDelay,
			final int intervalValue) {
		
		setUpVariables(directoryPath, directorys, initialDelay, intervalValue);
	}
	
	/**
	 * @param directoryPath
	 * @param directorys
	 * @param initialDelay
	 * @param intervalValue
	 */
	private void setUpVariables(final String directoryPath, final String directorys, final int initialDelay,
			final int intervalValue) {
		
		this.directoryPath = directoryPath;
		this.directorys = directorys;
		this.initialDelay = initialDelay;
		this.intervalValue = intervalValue;
		
		populateConfigMap();
	}
	
	private void populateConfigMap() {
		
		configMap = new HashMap<String, Object>();
		
		configMap.put(PARENT_DIRECTORY_PATH_PROP_NAME, directoryPath);
		configMap.put(DIRECTORY_LIST_PROP_NAME, directorys);
		configMap.put(INITIAL_DELAY_PROP_NAME, initialDelay);
		configMap.put(INTERVAL_VALUE_PROP_NAME, intervalValue);
		
	}
	
	@Override
	public Integer getIntProperty(final String propertyName) {
		return null;
	}
	
	@Override
	public String getStringProperty(final String propertyName) {
		
		for (final String configParam : configMap.keySet()) {
			if (configParam == propertyName) {
				return "" + configMap.get(configParam);
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
		return configMap;
	}
	
}
