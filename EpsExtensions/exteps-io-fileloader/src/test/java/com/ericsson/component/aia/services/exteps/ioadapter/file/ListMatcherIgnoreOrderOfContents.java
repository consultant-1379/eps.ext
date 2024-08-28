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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

/**
 * A Hamcrest Matcher that allows us to compare two list, without caring about the order of the elements in the list. eg
 * List A {A,B,C} = List B {B,A,C} There are only a few scenarios where you would want to check that two lists are equal
 * in this way If the order is unimportant, you might consider replacing the List with a Set in your implementation
 */

public class ListMatcherIgnoreOrderOfContents {
	public static <T> Matcher<List<T>> contentsTheSame(final List<T> expectedList) {
		return new BaseMatcher<List<T>>() {
			@Override
			public boolean matches(final Object object) {
				if (object instanceof List<?>) {
					final List<T> actualList = (List<T>) object;
					final Set<T> expectedSet = new HashSet<T>(expectedList);
					final Set<T> actualSet = new HashSet<T>(actualList);
					return actualSet.equals(expectedSet);
				}
				return false;
			}
			
			@Override
			public void describeTo(final Description description) {
				description.appendText("should contain all and only elements of ").appendValue(expectedList);
			}
		};
	}
}