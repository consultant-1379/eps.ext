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

import com.ericsson.component.aia.services.exteps.io.adapter.streaming.ReactiveStreamingInputAdapter;
import com.ericsson.component.aia.itpf.common.event.ControlEvent;

public class ReactiveStreamingInputAdapterExt extends ReactiveStreamingInputAdapter{
    
    private ControlEvent controlEvent;
    public void setControlEvent(final ControlEvent controlEvent){
        this.controlEvent = controlEvent;
    }
    public ControlEvent getControlEvent(){
        return controlEvent;
    }
    
    @Override
    public void react(final ControlEvent controlEvent) {
        this.controlEvent = controlEvent;
    }

}
