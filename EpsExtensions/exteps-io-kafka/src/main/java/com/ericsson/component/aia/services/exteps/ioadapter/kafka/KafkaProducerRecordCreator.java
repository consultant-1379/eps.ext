/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.exteps.ioadapter.kafka;

import org.apache.kafka.clients.producer.ProducerRecord;

/**
 * Interface to be implemented when using {@link KafkaOutputAdapter}. Implementation decides for every event where to send it (topic name, partition,
 * key) and also what to send (format).
 *
 * @author eborziv
 *
 */
public interface KafkaProducerRecordCreator {

    public ProducerRecord getProducerRecordForEvent(Object event);

}