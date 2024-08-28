/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2018
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.exteps.eh.parser.exception;

/**
 * EventFilterServiceLocatorException used as an exception class for event service locator.
 */
public class EventFilterServiceLocatorException extends RuntimeException {

    private static final long serialVersionUID = 6963511365217237970L;

    /**
     * Instantiates a new event filter service locator exception.
     *
     * @param message
     *            error message
     */
    public EventFilterServiceLocatorException(final String message) {
        super(message);
    }

    /**
     * Instantiates a new event filter service locator exception.
     *
     * @param message
     *            error message
     * @param throwable
     *            exception
     */
    public EventFilterServiceLocatorException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

}
