package com.ericsson.component.aia.services.exteps.event.router;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.ericsson.component.aia.services.exteps.event.router.StrategyBasedEventRouter;
import com.ericsson.component.aia.services.exteps.event.router.util.StubbedConfiguration;
import com.ericsson.component.aia.services.exteps.event.router.util.StubbedContext;
import com.ericsson.component.aia.services.exteps.event.router.util.StubbedEventSubscriber;

public class TestEventRouterWithRoundRobinRoutingStrategy {

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
    public void shouldResolveStrategyRoundRobin() {
        eventHandlerConfiguration.setStringProperty("eventRoutingStrategy", "ROUND_ROBIN");
        final StubbedEventSubscriber eventSubscriber1 = new StubbedEventSubscriber();
        final StubbedEventSubscriber eventSubscriber2 = new StubbedEventSubscriber();
        final StubbedEventSubscriber eventSubscriber3 = new StubbedEventSubscriber();
        final StubbedEventSubscriber eventSubscriber4 = new StubbedEventSubscriber();
        final StubbedEventSubscriber eventSubscriber5 = new StubbedEventSubscriber();
        eventHandlerContext.addEventSubscriber(eventSubscriber1);
        eventHandlerContext.addEventSubscriber(eventSubscriber2);
        eventHandlerContext.addEventSubscriber(eventSubscriber3);
        eventHandlerContext.addEventSubscriber(eventSubscriber4);
        eventHandlerContext.addEventSubscriber(eventSubscriber5);
        eventRouter.init(eventHandlerContext);
        eventRouter.onEvent("sample Message");
        eventRouter.onEvent("sample Message");
        eventRouter.onEvent("sample Message");
        eventRouter.onEvent("sample Message");
        eventRouter.onEvent("sample Message");
        Assert.assertEquals(1, eventSubscriber1.getEvents().size());
        Assert.assertEquals(1, eventSubscriber2.getEvents().size());
        Assert.assertEquals(1, eventSubscriber3.getEvents().size());
        Assert.assertEquals(1, eventSubscriber4.getEvents().size());
        Assert.assertEquals(1, eventSubscriber5.getEvents().size());
        eventRouter.onEvent("sample Message");
        Assert.assertEquals(2, eventSubscriber1.getEvents().size());
        eventRouter.onEvent("sample Message");
        Assert.assertEquals(2, eventSubscriber2.getEvents().size());
        eventRouter.onEvent("sample Message");
        Assert.assertEquals(2, eventSubscriber3.getEvents().size());
        eventRouter.onEvent("sample Message");
        Assert.assertEquals(2, eventSubscriber4.getEvents().size());
        eventRouter.onEvent("sample Message");
        Assert.assertEquals(2, eventSubscriber5.getEvents().size());
        eventRouter.onEvent("sample Message");
        Assert.assertEquals(3, eventSubscriber1.getEvents().size());
    }

}
