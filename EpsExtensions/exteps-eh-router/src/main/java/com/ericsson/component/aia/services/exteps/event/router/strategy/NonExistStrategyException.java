package com.ericsson.component.aia.services.exteps.event.router.strategy;

/**
 *
 * this exception raised, if handle for the routing strategy is not matching with any of the loaded routing strategy
 *
 */
public class NonExistStrategyException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NonExistStrategyException() {

    }

    public NonExistStrategyException(final String reason) {
        super(reason);
    }

}
