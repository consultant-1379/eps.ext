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

import static org.junit.Assert.assertThat;

import org.hamcrest.core.IsInstanceOf;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.mediation.event.service.FileEventFilterService;
import com.ericsson.component.aia.mediation.event.service.FilterEventReader;
import com.ericsson.component.aia.mediation.event.service.RestEventFilterService;
import com.ericsson.component.aia.services.exteps.eh.parser.exception.EventFilterServiceLocatorException;

/**
 * EventFilterServiceLocatorHelperTest verifies positive and negative test cases for EventFilterServiceLocatorHelper class
 */
public class EventFilterServiceLocatorHelperTest {

    protected static final Logger logger = LoggerFactory.getLogger(EventFilterServiceLocatorHelperTest.class);
    private static String FAILED_ERROR_MESSAGE = "helper must return valid object";

    @Test
    public void validateEventReaderSPI_validURI_retValidEventReader() {
        FilterEventReader eventReader = null;
        try {
            eventReader = EventFilterServiceLocatorHelper.validateEventReaderSPI("local://ericsson/config/eventfilter.json");
        } catch (final EventFilterServiceLocatorException e) {
            logger.error(FAILED_ERROR_MESSAGE);
            Assert.fail(FAILED_ERROR_MESSAGE);
        }

        assert (eventReader != null);
        assertThat(eventReader, IsInstanceOf.instanceOf(FileEventFilterService.class));
    }

    @Test
    public void validateEventReaderSPI_invalidURI_retValidEventReader() {
        FilterEventReader eventReader = null;
        try {
            eventReader = EventFilterServiceLocatorHelper.validateEventReaderSPI("http1://ericsson/config/eventfilter.json");
        } catch (final EventFilterServiceLocatorException e) {
            logger.error(FAILED_ERROR_MESSAGE);
            Assert.fail(FAILED_ERROR_MESSAGE);
        }

        assert (eventReader == null);
    }

    @Test
    public void validateEventReaderSPI_validRestURI_retValidEventReader() {
        FilterEventReader eventReader = null;
        try {
            eventReader = EventFilterServiceLocatorHelper.validateEventReaderSPI("http://ericsson/config/eventfilter.json");
        } catch (final EventFilterServiceLocatorException e) {
            logger.error(FAILED_ERROR_MESSAGE);
            Assert.fail(FAILED_ERROR_MESSAGE);
        }
        assert (eventReader != null);
        assertThat(eventReader, IsInstanceOf.instanceOf(RestEventFilterService.class));
    }

}
