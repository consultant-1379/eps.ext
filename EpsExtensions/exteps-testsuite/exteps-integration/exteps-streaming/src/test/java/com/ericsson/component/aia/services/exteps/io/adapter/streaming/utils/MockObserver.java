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
package com.ericsson.component.aia.services.exteps.io.adapter.streaming.utils;

import java.io.Serializable;

import com.ericsson.component.aia.itpf.common.Controllable;
import com.ericsson.component.aia.itpf.common.event.ControlEvent;
import com.ericsson.oss.itpf.sdk.cluster.coordination.application.NodeObserver;
import com.ericsson.oss.itpf.sdk.cluster.coordination.application.NodeType;
import com.ericsson.component.aia.services.eps.coordination.EpsAdaptiveConfiguration;
import com.ericsson.component.aia.services.eps.coordination.XMLDeSerializer;

public class MockObserver implements NodeObserver {

    private Controllable controllableComponent;

    public MockObserver(){

    }

    public MockObserver(final Controllable controller){
        this.controllableComponent = controller;
    }

    @Override
    public void onCreate(final NodeType nodeType, final String path, final Serializable value) {
        handle(value);
    }

    @Override
    public void onRemove(final NodeType nodeType, final String path) {
        handle(null);

    }

    @Override
    public void onUpdate(final NodeType nodeType, final String path, final Serializable value) {
        handle(value);
    }

    private void handle(final Serializable data) {
        final ControlEvent controlEvent = new ControlEvent(ControlEvent.CONFIGURATION_CHANGED);
        if(data == null){
            return;
        }
        if(!(data instanceof String)){
            return;
        }
        final EpsAdaptiveConfiguration epsAdaptiveConfiguration = convertToHandlerConfiguration(data); 
        updateControlEvent(controlEvent,  epsAdaptiveConfiguration);
        reactAndSaveStatus(controlEvent);
    }

    private void reactAndSaveStatus(final ControlEvent controlEvent) {
        controllableComponent.react(controlEvent);
    }

    private static EpsAdaptiveConfiguration convertToHandlerConfiguration(final Serializable data) {
        return XMLDeSerializer.unmarshal((String)data, EpsAdaptiveConfiguration.class);
    }

    private void updateControlEvent(final ControlEvent controlEvent, final EpsAdaptiveConfiguration handlerConfiguration) {
        if(handlerConfiguration.getConfiguration() == null){
            return;
        }
        controlEvent.getData().putAll(handlerConfiguration.getConfiguration()); 
    }


}
