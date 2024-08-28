package com.ericsson.component.aia.services.exteps.eh.router;

import com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler;
import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;
import com.ericsson.component.aia.mediation.parsers.streamrecord.StreamedRecord;

/**
 *
 * Selects the Event Handler to send received StreamedRecord events to, based
 * on the event IMSI value.
 *
 * Events with the same IMSI will always be sent to the same subscriber.
 *
 * Only applicable where the received event is an instance of StreamedRecord,
 * any other events are discarded.
 */
public class StreamedRecordRouter extends AbstractEventHandler implements
        EventInputHandler {

    @Override
    protected void doInit() {
        if (getNumberOfSubscribers() < 2) {
            log.error(
                    "Found {} subscribers for router! Routing might not be optimal! It should be used where there is at least 2 subscribers",
                    getNumberOfSubscribers());
        }
    }

    @Override
    public void onEvent(final Object inputEvent) {
        if (inputEvent instanceof StreamedRecord) {
            final StreamedRecord record = (StreamedRecord) inputEvent;
            final int sourceId = record.getSourceId();
            final int route = getStreamRecordRoute(sourceId,
                    getNumberOfSubscribers());
            if (route < 0) {
                log.warn(
                        "Calculated route is negative and it must not be! sourceId={}, numberOfSubscribers={}",
                        sourceId, getNumberOfSubscribers());
            } else {
                sendEvent(inputEvent, route);
            }
        } else {
            log.warn("Unable to route unknown event {}", inputEvent);
        }
    }

    private int getStreamRecordRoute(final int sourceId,
            final int numberOfRoutes) {
        if (numberOfRoutes <= 0) {
            log.warn("There is no attached subscribers.");
            return -1;
        }
        final int route = (sourceId % numberOfRoutes);
        log.debug("Route for sourceId={} routes to {} - number of routes = {}",
                sourceId, route, numberOfRoutes);
        return route;
    }

}
