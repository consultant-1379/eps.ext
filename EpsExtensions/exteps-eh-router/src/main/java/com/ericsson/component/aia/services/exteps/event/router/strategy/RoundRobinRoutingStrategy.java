package com.ericsson.component.aia.services.exteps.event.router.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * delivers event as round robin cycle
 *
 */
public class RoundRobinRoutingStrategy extends AbstractEventRoutingStrategy {

    static final Logger logger = LoggerFactory.getLogger(RoundRobinRoutingStrategy.class);

    private int index = 0;

    public RoundRobinRoutingStrategy() {
        name = "ROUND_ROBIN";
    }

    @Override
    public int getRouteId(final Object event) {
        if (index == numberOfSubscribers) {
            index = 0;
        }
        return index++;
    }

}
