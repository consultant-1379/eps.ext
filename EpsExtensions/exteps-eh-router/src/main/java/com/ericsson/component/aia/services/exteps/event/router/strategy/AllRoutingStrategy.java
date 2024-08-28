package com.ericsson.component.aia.services.exteps.event.router.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * routes event to all subscribers
 *
 */
public class AllRoutingStrategy extends AbstractEventRoutingStrategy {

    static final Logger logger = LoggerFactory.getLogger(AllRoutingStrategy.class);

    public AllRoutingStrategy() {
        name = "ALL";
    }

    @Override
    public int getRouteId(final Object event) {
        return ALL_SUBSCRIBERS;
    }

}