package com.ericsson.component.aia.services.exteps.event.router;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.ericsson.component.aia.services.exteps.event.router.StrategyBasedEventRouter;
import com.ericsson.component.aia.services.exteps.event.router.util.StubbedConfiguration;
import com.ericsson.component.aia.services.exteps.event.router.util.StubbedContext;
import com.ericsson.component.aia.services.exteps.event.router.util.StubbedEventSubscriber;

public class TestEventRouterWithAllRoutingStrategy {

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
    public void shouldResolveStrategyAll() {
        eventHandlerConfiguration.setStringProperty("eventRoutingStrategy", "ALL");
        assertAll();
    }

    @Test
    public void shouldResolveStrategyAllWhenDefault() {
        assertAll();
    }

    private void assertAll() {
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
        Assert.assertEquals(eventSubscriber1.getEvents().size(), 3);
        Assert.assertEquals(eventSubscriber2.getEvents().size(), 3);
        Assert.assertEquals(eventSubscriber3.getEvents().size(), 3);
        Assert.assertEquals(eventSubscriber4.getEvents().size(), 3);
        Assert.assertEquals(eventSubscriber5.getEvents().size(), 3);
    }
}
