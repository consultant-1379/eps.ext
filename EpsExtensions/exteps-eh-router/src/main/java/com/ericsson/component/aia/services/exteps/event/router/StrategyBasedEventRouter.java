package com.ericsson.component.aia.services.exteps.event.router;

import static com.ericsson.component.aia.services.exteps.event.router.strategy.EventRoutingStrategy.ALL_SUBSCRIBERS;
import static com.ericsson.component.aia.services.exteps.event.router.strategy.EventRoutingStrategy.NONE_SUBSCRIBER;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.exteps.event.router.strategy.EventRoutingStrategy;
import com.ericsson.component.aia.services.exteps.event.router.strategy.RoutingStrategyFactory;
import com.ericsson.component.aia.services.exteps.event.router.strategy.RoutingStrategyInitializationException;
import com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler;
import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;

/**
 *
 * This handler Routes(Directs) events to the subscribed handlers(subscribers) inside the path including router.<br>
 * May work based on the incoming event and configured attributes(such as properties, expression)<br>
 *
 * eventRoutingStrategy has to be defined<br>
 * eventRoutingStrategy will provide handle for routing strategy. <br>
 * if eventRoutingStrategy specified Default Routing strategy is to send events to ALL subscribed subscribers <br>
 * if eventRoutingStrategy wrongly defined then NonExistStrategyException will be thrown. <br>
 * any number of attributes can exist in the flow step <br>
 * all defined attributes will be passed to related routing strategy as it is DO NOT PASS any attribute if routing strategy does not require.<br>
 * This will have no affect.<br>
 *
 * Routing strategy is pluggable. <br>
 * Either one of the built-in Strategy can be used or separate jar containing META-INF/services can be attached.<br>
 * Correct routing strategy is injected with 'eventRoutingStrategy' to the name attribute in the flow xml.<br>
 * Routing Strategy has to be defined as SPI.<br>
 * com.ericsson.component.aia.services.exteps.event.router.strategy.EventRoutingStrategy should be implemented.<br>
 *
 *
 * If provided Routing Strategy is not found, NonExistStrategyException is thrown. <br>
 * if you would like to write own RoutingStrategy, this can be provided in any Jar as long as SPI defined.<br>
 *
 * RouteId cannot exceed numberofSubscribers.<br>
 *
 * Built-in Routing Strategy List here.<br>
 *
 *
 * AllRoutingStrategy --> ALL* (Default One.) <br>
 * NoneRoutingStrategy --> NONE <br>
 * RoundRobinRoutingStrategy --> ROUND_ROBIN <br>
 * ShuffledRoutingStrategy --> SHUFFLED <br>
 * PatternDrivenRoutingStrategy --> PATTERN
 *
 */
public class StrategyBasedEventRouter extends AbstractEventHandler implements EventInputHandler {

    static final Logger logger = LoggerFactory.getLogger(StrategyBasedEventRouter.class);

    private static final String STRATEGY = "eventRoutingStrategy";
    private static final String DEFAULT_ROUTING_STRATEGY = "ALL";

    private EventRoutingStrategy routingStrategy;
    private int numberOfSubscribers;

    @Override
    protected void doInit() {
        String routingStrategyName = getConfiguration().getStringProperty(STRATEGY);
        if (routingStrategyName == null) {
            routingStrategyName = DEFAULT_ROUTING_STRATEGY;
        }
        routingStrategy = RoutingStrategyFactory.injectNewStrategy(routingStrategyName);
        if (routingStrategy == null) {
            throw new RoutingStrategyInitializationException("Unable to inject strategy to the StrategyBasedEventRouter");
        }
        numberOfSubscribers = getNumberOfSubscribers();
        logger.info("Event Router will be initialized with the parameters RoutingStrategyName:[{}]", routingStrategyName);
        routingStrategy.initialize(getConfiguration().getAllProperties(), numberOfSubscribers);
        logger.info("Event Router initialized with the parameters RoutingStrategyName:[{}]", routingStrategyName);
    }

    @Override
    public void onEvent(final Object inputEvent) {
        if (inputEvent == null) {
            logger.trace("Null inputEvent ignored.");
            return;
        }
        log(inputEvent);
        publish(inputEvent);
    }

    private void publish(final Object inputEvent) {
        final int routeId = getRoute(inputEvent);
        if (routeId == NONE_SUBSCRIBER) {
            logNone();
            return;
        }

        if (routeId == ALL_SUBSCRIBERS) {
            logAll();
            sendToAllSubscribers(inputEvent);
            return;
        }

        sendEventToRoute(inputEvent, routeId);
    }

    private void sendEventToRoute(final Object inputEvent, final int routeId) {
        if (isValidRouteId(routeId)) {
            logRoute(routeId);
            sendEvent(inputEvent, routeId);
        } else {
            logger.error("route Id [{}] is invalid, should be between 0-[] ", routeId, numberOfSubscribers);
        }
    }

    private boolean isValidRouteId(final int routeId) {
        return routeId > -3 && routeId < numberOfSubscribers;
    }

    private void logRoute(final int routeId) {
        logger.trace("event to route [{}]", routeId);
    }

    private void logNone() {
        logger.trace("skipping event as routing strategy defined as NONE");
    }

    private void logAll() {
        logger.trace("sending event to all subscribers as routing strategy defined as ALL");
    }

    private int getRoute(final Object inputEvent) {
        final int routeId = routingStrategy.getRouteId(inputEvent);
        logger.trace("subscriber route calculated as [{}] ", routeId);
        return routeId;
    }

    private void log(final Object inputEvent) {
        if (logger.isTraceEnabled()) {
            logger.trace("event received [{}]", inputEvent.toString());
        }
    }

}
