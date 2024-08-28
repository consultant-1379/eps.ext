package com.ericsson.component.aia.services.exteps.event.router;

import org.junit.Before;
import org.junit.Test;

import com.ericsson.component.aia.services.exteps.event.router.StrategyBasedEventRouter;
import com.ericsson.component.aia.services.exteps.event.router.strategy.NonExistStrategyException;
import com.ericsson.component.aia.services.exteps.event.router.util.StubbedConfiguration;
import com.ericsson.component.aia.services.exteps.event.router.util.StubbedContext;

public class TestEventRouter {

    private StrategyBasedEventRouter eventRouter;
    private StubbedContext eventHandlerContext;
    private StubbedConfiguration eventHandlerConfiguration;

    @Before
    public void setup() {
        eventHandlerConfiguration = new StubbedConfiguration();
        eventHandlerContext = new StubbedContext(eventHandlerConfiguration);
        eventRouter = new StrategyBasedEventRouter();
    }

    @Test(expected = NonExistStrategyException.class)
    public void shouldThrowStrategyNotFoundException() {
        eventHandlerConfiguration.setStringProperty("eventRoutingStrategy", "XYZ2");
        eventRouter.init(eventHandlerContext);
        eventRouter.doInit();
    }

    @Test
    public void shouldUseDefault() {
        eventRouter.init(eventHandlerContext);
        eventRouter.doInit();
    }

    @Test
    public void shouldResolveStrategyAll() {
        eventHandlerConfiguration.setStringProperty("eventRoutingStrategy", "ALL");
        eventRouter.init(eventHandlerContext);
    }

    @Test
    public void shouldResolveStrategyNone() {
        eventHandlerConfiguration.setStringProperty("eventRoutingStrategy", "NONE");
        eventRouter.init(eventHandlerContext);
    }

    @Test
    public void shouldResolveStrategyRoundRobin() {
        eventHandlerConfiguration.setStringProperty("eventRoutingStrategy", "ROUND_ROBIN");
        eventRouter.init(eventHandlerContext);
    }

    @Test
    public void shouldResolveStrategyPattern() {
        eventHandlerConfiguration.setStringProperty("eventRoutingStrategy", "PATTERN");
        eventHandlerConfiguration.setStringProperty("patternToEvaluate", "IMSI");
        eventRouter.init(eventHandlerContext);
    }

    @Test
    public void shouldResolveStrategyShuffled() {
        eventHandlerConfiguration.setStringProperty("eventRoutingStrategy", "SHUFFLED");
        eventRouter.init(eventHandlerContext);
    }

}
