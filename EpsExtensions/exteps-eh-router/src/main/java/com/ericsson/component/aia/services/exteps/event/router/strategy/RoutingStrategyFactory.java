package com.ericsson.component.aia.services.exteps.event.router.strategy;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * loads all the routing strategies exist under META-INF/services folder
 * com.ericsson.component.aia.services.exteps.event.router.strategy.EventRoutingStrategy
 * file.
 *
 */
public class RoutingStrategyFactory {

    static final Logger logger = LoggerFactory.getLogger(RoutingStrategyFactory.class);
    private static final Map<String, EventRoutingStrategy> routingStrategyMap;

    static {
        routingStrategyMap = new HashMap<String, EventRoutingStrategy>();
        final ServiceLoader<EventRoutingStrategy> loader = ServiceLoader.load(EventRoutingStrategy.class);
        for (final EventRoutingStrategy routingStrategy : loader) {
            routingStrategyMap.put(routingStrategy.getName(), routingStrategy);
        }
        if (routingStrategyMap.isEmpty()) {
            logger.error("No routing strategy exists");
        }
    }

    /**
     * Creates a new instance of the requested RoutingStrategy.
     *
     * @param routingStrategyName the requested RoutingStrategy class name
     * @return a new instance of the requested RoutingStrategy, or null if the RoutingStrategy class
     *         cannot be instantiated.
     * @throws NonExistStrategyException
     *         if no implementation found for the requested routingStrategyName.
     *
     */
    public static EventRoutingStrategy injectNewStrategy(final String routingStrategyName) {
        final EventRoutingStrategy routingStrategy = routingStrategyMap.get(routingStrategyName);
        if (routingStrategy == null) {
            throw new NonExistStrategyException("There is no strategy exist with the name : " + routingStrategyName
                    + " check /META-INF/services/com.ericsson.component.aia.services.exteps.event.router.strategy.EventRoutingStrategy");
        }
        try {
            // Multiple instances of a RoutingStrategy may be used in the same jvm.
            return routingStrategy.getClass().newInstance();
        } catch (final IllegalAccessException | InstantiationException exception) {
            logger.error("Error occured during copy of EventRoutingStrategy [{}] ", exception);
        }

        return null;

    }
}
