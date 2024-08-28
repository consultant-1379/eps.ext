package com.ericsson.component.aia.services.exteps.event.router.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * randomly selects one route.
 *
 */
public class ShuffledRoutingStrategy extends AbstractEventRoutingStrategy {

    static final Logger logger = LoggerFactory.getLogger(ShuffledRoutingStrategy.class);

    public ShuffledRoutingStrategy() {
        name = "SHUFFLED";
    }

    @Override
    public int getRouteId(final Object event) {
        final int randomNum = 0 + (int) (Math.random() * (numberOfSubscribers - 1));
        return Math.abs(randomNum);
    }

}
