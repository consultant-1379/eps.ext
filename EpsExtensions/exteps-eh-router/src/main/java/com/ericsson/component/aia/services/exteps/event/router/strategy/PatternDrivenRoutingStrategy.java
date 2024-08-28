package com.ericsson.component.aia.services.exteps.event.router.strategy;

import static com.ericsson.component.aia.services.exteps.event.router.strategy.RoutingUtil.print;

import java.io.Serializable;
import java.util.Map;

import org.mvel2.MVEL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Requires a sample attribute like this name="patternToEvaluate" value="ENB_UE_S1AP_ID#MME_UE_S1AP_ID#ENODEB_ID_MACRO_ENODEB_ID" <br>
 *
 * Apply specified pattern(like the one above) to incoming event
 *
 */
public class PatternDrivenRoutingStrategy extends AbstractEventRoutingStrategy {

    static final Logger logger = LoggerFactory.getLogger(PatternDrivenRoutingStrategy.class);
    private static final String PROPERTY = "patternToEvaluate";

    private Serializable compiledExpression;

    public PatternDrivenRoutingStrategy() {
        name = "PATTERN";
    }

    @Override
    public void initialize(final Map<String, Object> propertyMap, final int numberOfSubscribers) {
        logger.info("[{}] STRATEGY initializing with parameters : [{}], NUMBER_OF_SUBSCRIBERS [{}] ", getName(), print(propertyMap),
                numberOfSubscribers);
        this.numberOfSubscribers = numberOfSubscribers;
        final String expression = (String) propertyMap.get(PROPERTY);
        if (expression == null || "".equalsIgnoreCase(expression.trim())) {
            throw new RoutingStrategyInitializationException("an expression has to be defined for PatternDrivenRoutingStrategy");
        }
        compiledExpression = MVEL.compileExpression(expression);
    }

    @Override
    public int getRouteId(final Object event) {
        long returnValue = 1;
        try {
            returnValue = getLongValue(event);
        } catch (final Exception exception) {
            logger.error("Exception while calculating the route [{}]", exception);
        }

        return Math.abs((int) returnValue % numberOfSubscribers);

    }

    private Long getLongValue(final Object event) {
        long returnValue = 1;
        try {
            returnValue = (Long) MVEL.executeExpression(compiledExpression, event);
        } catch (final ClassCastException e) {
            returnValue = ((String) MVEL.executeExpression(compiledExpression, event)).hashCode();
        }
        return returnValue;
    }

}
