package com.ericsson.component.aia.services.exteps.event.router.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * skip the event(does not route to none of the subscribers)
 *
 */
public class NoneRoutingStrategy extends AbstractEventRoutingStrategy {

    static final Logger logger = LoggerFactory.getLogger(NoneRoutingStrategy.class);

    public NoneRoutingStrategy() {
        name = "NONE";
    }

    @Override
    public int getRouteId(final Object event) {
        return NONE_SUBSCRIBER;
    }

}
