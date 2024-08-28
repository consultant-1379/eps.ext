
package com.ericsson.component.aia.services.exteps.io.adapter.streaming.exceptions;

public class ClientNotConnectedException extends Exception {

    private static final long serialVersionUID = 7118108645012443331L;

    public ClientNotConnectedException() {
        super();
    }

    public ClientNotConnectedException(final String message) {
        super(message);
    }

}
