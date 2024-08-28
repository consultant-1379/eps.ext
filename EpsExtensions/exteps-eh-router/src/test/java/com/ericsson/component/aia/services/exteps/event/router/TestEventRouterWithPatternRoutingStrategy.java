package com.ericsson.component.aia.services.exteps.event.router;

import java.io.Serializable;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mvel2.MVEL;

import com.ericsson.component.aia.services.exteps.event.router.StrategyBasedEventRouter;
import com.ericsson.component.aia.services.exteps.event.router.util.SamplePojo;
import com.ericsson.component.aia.services.exteps.event.router.util.StubbedConfiguration;
import com.ericsson.component.aia.services.exteps.event.router.util.StubbedContext;
import com.ericsson.component.aia.services.exteps.event.router.util.StubbedEventSubscriber;

public class TestEventRouterWithPatternRoutingStrategy {

    private StrategyBasedEventRouter eventRouter;
    private StubbedContext eventHandlerContext;
    private StubbedConfiguration eventHandlerConfiguration;

    @Before
    public void setup() {
        eventHandlerConfiguration = new StubbedConfiguration();
        eventHandlerContext = new StubbedContext(eventHandlerConfiguration);
        eventRouter = new StrategyBasedEventRouter();
    }

    @Test
    public void shouldResolveStrategyPattern() {
        final String patternToEvaluate = "ENB_UE_S1AP_ID#MME_UE_S1AP_ID#ENODEB_ID_MACRO_ENODEB_ID";
        final Serializable serializedExpression = MVEL.compileExpression(patternToEvaluate);
        eventHandlerConfiguration.setStringProperty("eventRoutingStrategy", "PATTERN");
        eventHandlerConfiguration.setStringProperty("patternToEvaluate", patternToEvaluate);
        final StubbedEventSubscriber eventSubscriber1 = new StubbedEventSubscriber();
        final StubbedEventSubscriber eventSubscriber2 = new StubbedEventSubscriber();
        final StubbedEventSubscriber eventSubscriber3 = new StubbedEventSubscriber();
        final StubbedEventSubscriber eventSubscriber4 = new StubbedEventSubscriber();
        final StubbedEventSubscriber eventSubscriber5 = new StubbedEventSubscriber();
        final StubbedEventSubscriber[] subscriberArray = new StubbedEventSubscriber[] { eventSubscriber1, eventSubscriber2, eventSubscriber3,
                eventSubscriber4, eventSubscriber5 };
        eventHandlerContext.addEventSubscriber(eventSubscriber1);
        eventHandlerContext.addEventSubscriber(eventSubscriber2);
        eventHandlerContext.addEventSubscriber(eventSubscriber3);
        eventHandlerContext.addEventSubscriber(eventSubscriber4);
        eventHandlerContext.addEventSubscriber(eventSubscriber5);
        eventRouter.init(eventHandlerContext);
        final SamplePojo event1 = new SamplePojo(0l, 0l, 0l);
        assertCorrectRoute(serializedExpression, subscriberArray, event1);
        final SamplePojo event2 = new SamplePojo(2l, 2l, 2l);
        assertCorrectRoute(serializedExpression, subscriberArray, event2);
        final SamplePojo event3 = new SamplePojo(1l, 1l, 1l);
        assertCorrectRoute(serializedExpression, subscriberArray, event3);
        final SamplePojo event4 = new SamplePojo(3l, 1l, 3l);
        assertCorrectRoute(serializedExpression, subscriberArray, event4);
        final SamplePojo event5 = new SamplePojo(3l, 3l, 3l);
        assertCorrectRoute(serializedExpression, subscriberArray, event5);
    }

    private void assertCorrectRoute(final Serializable serializedExpression, final StubbedEventSubscriber[] subscriberArray, final SamplePojo event) {
        final int routeId = Math.abs((int) (getLongValue(event, serializedExpression) % 5));
        eventRouter.onEvent(event);
        Assert.assertEquals(subscriberArray[routeId].getEvents().size(), 1);
        subscriberArray[routeId].getEvents().clear();
    }

    private static Long getLongValue(final Object event, final Serializable expression) {
        long returnValue = 1;
        try {
            returnValue = (Long) MVEL.executeExpression(expression, event);
        } catch (final ClassCastException e) {
            returnValue = ((String) MVEL.executeExpression(expression, event)).hashCode();
        }
        return returnValue;
    }
}
