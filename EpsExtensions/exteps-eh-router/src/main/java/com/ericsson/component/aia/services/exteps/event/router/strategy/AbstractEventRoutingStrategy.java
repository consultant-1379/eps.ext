package com.ericsson.component.aia.services.exteps.event.router.strategy;

import static com.ericsson.component.aia.services.exteps.event.router.strategy.RoutingUtil.print;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractEventRoutingStrategy implements EventRoutingStrategy {

    static final Logger logger = LoggerFactory.getLogger(AbstractEventRoutingStrategy.class);

    protected String name;
    protected int numberOfSubscribers;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void initialize(final Map<String, Object> propertyMap, final int numberOfSubscribers) {
        logger.info("[{}] STRATEGY initializing with parameters : [{}], NUMBER_OF_SUBSCRIBERS [{}] ", getName(), print(propertyMap),
                numberOfSubscribers);
        this.numberOfSubscribers = numberOfSubscribers;
    }

}
