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
 * This class creates EPS event handler context to be used in {@link FileCollectionInputAdapterTest}.
 * 
 * @since 0.0.1-SNAPSHOT
 */
public class FileCollectionEventHandlerContextTest implements EventHandlerContext, Configuration {
	
	private String directoryPath;
	private String directorys;
	private int initialDelay;
	private int intervalValue;
	
	public FileCollectionEventHandlerContextTest(final String directoryPath, final String directorys,
			final int initialDelay, final int intervalValue) {
		
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
	public Configuration getEventHandlerConfiguration() {
		return new FileCollectionConfigurationTest(directoryPath, directorys, initialDelay, intervalValue);
	}
	
	@Override
	public Collection<EventSubscriber> getEventSubscribers() {
		return null;
	}
	
	@Override
	public void sendControlEvent(final ControlEvent controlEvent) {
		
	}
	
	@Override
	public Object getContextualData(final String name) {
		return null;
	}
	
}
