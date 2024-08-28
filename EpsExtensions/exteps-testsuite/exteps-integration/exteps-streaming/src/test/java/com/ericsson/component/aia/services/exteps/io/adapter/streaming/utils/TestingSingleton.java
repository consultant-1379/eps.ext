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

import com.ericsson.component.aia.mediation.parsers.streamrecord.StreamedRecord;

public class TestingSingleton {
    private StreamedRecord record = new StreamedRecord(-1);
    private static final TestingSingleton SINGLETON = new TestingSingleton();
    private TestingSingleton(){
        
    }
    public static TestingSingleton getInstance(){
        return SINGLETON;
    }
    
    public void setRecord(final StreamedRecord record){
        this.record = record;
    }
    
    public StreamedRecord getRecord(){
        return record;
    }
}
