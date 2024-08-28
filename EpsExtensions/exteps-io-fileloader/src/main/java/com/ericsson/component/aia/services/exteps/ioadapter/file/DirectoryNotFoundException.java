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

/**
 * This class defines an exception to be thrown, when no directories have been found in {@link FileCollectionInputAdapter}.
 *
 * @since 0.0.1-SNAPSHOT
 */
public class DirectoryNotFoundException extends Exception {

    private static final long serialVersionUID = -1095637402620769122L;

    public DirectoryNotFoundException(final String message) {
        super(message);
    }

    public DirectoryNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public DirectoryNotFoundException(final Throwable cause) {
        super(cause);
    }

}
