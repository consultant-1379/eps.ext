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
package com.ericsson.component.aia.services.exteps.ioadapter.input;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.ericsson.component.aia.services.exteps.ioadapter.output.GenericOutputAdapterTest;

/**
 * @author eachsaj
 * May 8, 2016
 */
@RunWith(Suite.class)
@SuiteClasses({ GenericInputAdapterTest.class, GenericOutputAdapterTest.class })
public class AllTests {
	
}
