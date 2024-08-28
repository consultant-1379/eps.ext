package com.ericsson.component.aia.services.exteps.event.router.strategy;

import java.util.Map;

/**
 *
 * This interface should be implemented to provide new RoutingStrategy. Implementing class has to exist under META-INF/services directory in a file
 * named com.ericsson.component.aia.services.exteps.event.router.strategy.EventRoutingStrategy
 *
 */
public interface EventRoutingStrategy {

    String PROPERTY_TOKENIZER = ",";
    int ALL_SUBSCRIBERS = -1;
    int NONE_SUBSCRIBER = -2;

    /**
     * @return name of the routing strategy This name is key to retrieve the correct EventStrategy
     */
    String getName();

    /**
     * if number of subscribers is n, routeId returned has to be 0 to n-1 if number of subscriber is out of range then that event will be skipped and
     * logged as error. -1 is reserved route Id for ALL subscribers -2 is reserved route Id for NONE subscribers
     *
     * @param event
     * @return id of the subscriber that is consuming the event
     */
    int getRouteId(Object event);

    /**
     * @param propertyMap
     *            , map of properties helping to calculate the routeId e.g. expression, property, can be null/empty if routing strategy has no need.
     * @param numberOfSubscribers
     *            is the upper boundary for the routeId, routeId should be in the range between 0-[numberofSubscriber-1]
     *
     */
    void initialize(Map<String, Object> propertyMap, int numberOfSubscribers);

}
