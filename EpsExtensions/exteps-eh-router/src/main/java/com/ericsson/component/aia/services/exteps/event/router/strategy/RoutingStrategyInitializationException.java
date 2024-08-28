package com.ericsson.component.aia.services.exteps.event.router.strategy;

/**
 * raised if initialization of the routing strategy fails.
 *
 */
public class RoutingStrategyInitializationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RoutingStrategyInitializationException() {

    }

    public RoutingStrategyInitializationException(final String reason) {
        super(reason);
    }

}
