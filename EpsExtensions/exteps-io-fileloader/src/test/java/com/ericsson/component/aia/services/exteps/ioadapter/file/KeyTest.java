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

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.ericsson.component.aia.services.exteps.ioadapter.file.Key;

/**
 * This class is a test class for {@link Key}
 * 
 * @since 0.0.1-SNAPSHOT
 */
public class KeyTest {
	
	Key keyOne;
	Key keyTwo;
	Key keyThree;
	
	@Before
	public void setup() {
		
		final String fileNameOne = "filename1";
		final String fileNameThree = "filename1";
		
		final String fileNameTwo = "filename2";
		
		final long ropTimeOne = 11100021521l;
		final long ropTimeThree = 11100021521l;
		
		final long ropTimeTwo = 11100021520l;
		
		keyOne = new Key(fileNameOne, ropTimeOne);
		keyThree = new Key(fileNameThree, ropTimeThree);
		
		keyTwo = new Key(fileNameTwo, ropTimeTwo);
	}
	
	@Test
	public void equals_keyOneAndKeyThreeCompared_True() {
		
		Assert.assertTrue(keyOne.equals(keyThree));
	}
	
	@Test
	public void equals_keyOneAndKeyTwoCompared_False() {
		
		Assert.assertFalse(keyOne.equals(keyTwo));
	}
	
	@Test
	public void hashCode_keyOneAndKeyThreeCompared_True() {
		
		Assert.assertTrue(keyOne.hashCode() == keyThree.hashCode());
	}
	
	@Test
	public void hashCode_keyOneAndKeyTwoCompared_false() {
		
		Assert.assertFalse(keyOne.hashCode() == keyTwo.hashCode());
	}
	
}
