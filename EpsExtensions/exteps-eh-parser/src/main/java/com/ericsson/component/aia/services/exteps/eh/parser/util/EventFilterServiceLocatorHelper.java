/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2018
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.exteps.eh.parser.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.mediation.event.service.FilterEventReader;
import com.ericsson.component.aia.services.exteps.eh.parser.FileInformationRetrieverUtil;
import com.ericsson.component.aia.services.exteps.eh.parser.exception.EventFilterServiceLocatorException;

/**
 * EventFilterServiceLocatorUtil used as an exception class for event reader
 */
public class EventFilterServiceLocatorHelper {

    private static final Logger log = LoggerFactory.getLogger(FileInformationRetrieverUtil.class);

    /**
     * validateEventReaderSPI used to understand URI and also verifies uniqueness of service.
     *
     * @param uri
     *            uri
     * @return filter event reader
     * @throws EventFilterServiceLocatorException
     *             the event filter service locator exception
     */
    public static FilterEventReader validateEventReaderSPI(final String uri) throws EventFilterServiceLocatorException {
        try {
            log.info("trying to load SPI for given uri {}", uri);

            //call event filter service
            final ServiceLoader<FilterEventReader> loader = ServiceLoader.load(FilterEventReader.class);

            if (loader != null) {

                final Iterator<FilterEventReader> iter = loader.iterator();

                final List<FilterEventReader> resultantEventReader = new ArrayList<FilterEventReader>();

                if (iter != null) {

                    while (iter.hasNext()) {

                        final FilterEventReader eventReader = iter.next();
                        log.info("found eventReader is {}", eventReader.getClass());

                        log.info("calling Event Filter SPI understandURI {}", uri);

                        if (eventReader.understandsURI(uri)) {
                            resultantEventReader.add(eventReader);
                            log.info("found eventReader service {} for URI {} ", eventReader, uri);

                        }

                    }

                    if (resultantEventReader.size() > 1) {
                        final String errorMessage = String.format("cannot have muliple services implementations with same URI %s ", uri);
                        log.warn(errorMessage);
                        throw new EventFilterServiceLocatorException(errorMessage);
                    } else if (resultantEventReader.size() == 0) {
                        log.warn("service implementation not available for given uri {}", uri);
                        return null;
                    }

                    log.info("finished finding SPI for uri {}", uri);
                    return resultantEventReader.get(0);
                }
            }
        } catch (final Throwable t) {
            log.error("Caught Exception ... ", t);
            throw new EventFilterServiceLocatorException(t.getMessage());
        }
        return null;
    }
}
