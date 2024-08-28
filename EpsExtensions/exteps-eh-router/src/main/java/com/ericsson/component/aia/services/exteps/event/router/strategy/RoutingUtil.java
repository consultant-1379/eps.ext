package com.ericsson.component.aia.services.exteps.event.router.strategy;

import java.util.Map;

/**
 * provides utilities
 *
 */
public abstract class RoutingUtil {

    public static Object print(final Map<String, Object> propertyMap) {
        String propertyValues = "";
        if (propertyMap == null || propertyMap.isEmpty()) {
            return propertyValues;
        }
        for (final Map.Entry<String, Object> propertyEntry : propertyMap.entrySet()) {
            propertyValues += "<" + propertyEntry.getKey() + ":" + (propertyEntry.getValue() == null ? "" : propertyEntry.getValue().toString())
                    + ">";
        }
        return propertyValues;
    }

}
