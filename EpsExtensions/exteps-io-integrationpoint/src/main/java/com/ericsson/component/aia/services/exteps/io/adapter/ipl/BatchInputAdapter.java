/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.exteps.io.adapter.ipl;

import static com.ericsson.component.aia.services.exteps.io.adapter.common.Constants.*;

import com.ericsson.aia.ipl.EventSubscriber;
import com.ericsson.aia.ipl.factory.EventServiceFactory;
import com.ericsson.aia.ipl.impl.ProcessMode;
import com.ericsson.component.aia.itpf.common.config.Configuration;
import com.ericsson.component.aia.itpf.common.event.handler.*;
import com.ericsson.component.aia.itpf.common.io.InputAdapter;
import com.ericsson.component.aia.services.exteps.io.adapter.util.GlobalPropertiesRetrieverUtil;

/**
 * The Class BatchInputAdapter, responsible for consuming events.
 *
 * @param <V>
 *            the event type
 */
public class BatchInputAdapter<V> extends AbstractEventHandler implements InputAdapter {

    private static final int DEFAULT_BATCH_MS = 30000;
    private static final String URI = "batch:/";

    private EventSubscriber<V> subscriber;

    @Override
    public void doInit() {
        final String integrationPointUri = getConfig().getStringProperty(INTEGRATION_POINT_URI);
        final String integrationPointName = getConfig().getStringProperty(INTEGRATION_POINT_NAME);
        final int batchMs = (getConfig().getIntProperty(BATCH_MS) == null) ? DEFAULT_BATCH_MS : getConfig().getIntProperty(BATCH_MS);
        final String globalPropertiesFilePath = GlobalPropertiesRetrieverUtil.getFilePath(getConfig());
        final EventServiceFactory<V> factory = new EventServiceFactory<>(integrationPointUri, ProcessMode.BATCH, batchMs, globalPropertiesFilePath);
        this.subscriber = factory.createEventSubscriber(integrationPointName);
        subscriber.registerEventListener(new BatchEventListener<V>(this));
    }

    @Override
    public void onEvent(final Object inputEvent) {
        throw new UnsupportedOperationException(
                "Operation not supported. An input adapter should never be passed events from another event handler!");
    }

    @Override
    public boolean understandsURI(final String uri) {
        return uri != null && uri.startsWith(URI);
    }

    public EventSubscriber<V> getSubscriber() {
        return subscriber;
    }

    public Configuration getConfig() {
        return getConfiguration();
    }

    @Override
    public void destroy() {
        log.info("Shutting down {} ...", this.getClass().getCanonicalName());
        try {
            if (subscriber != null) {
                subscriber.close();
            }
        } catch (final Exception exception) {
            log.error("Unexpected exception while closing batch subscriber", exception);
        }
    }
}
