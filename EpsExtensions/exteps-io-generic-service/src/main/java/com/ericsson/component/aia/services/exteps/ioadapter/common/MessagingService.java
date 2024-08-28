/**
 *
 * (C) Copyright LM Ericsson System Expertise AT/LMI, 2016
 *
 * The copyright to the computer program(s) herein is the property of Ericsson System Expertise EEI, Sweden. The
 * program(s) may be used and/or copied only with the written permission from Ericsson System Expertise AT/LMI or in *
 * accordance with the terms and conditions stipulated in the agreement/contract under which the program(s) have been
 * supplied.
 *
 */
package com.ericsson.component.aia.services.exteps.ioadapter.common;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.exteps.ioadapter.builder.IO_CONSTANTS;
import com.ericsson.component.aia.services.exteps.ioadapter.dfconsumer.AvroDataCollector;
import com.ericsson.component.aia.services.exteps.ioadapter.dfconsumer.BinaryDataCollector;
import com.ericsson.component.aia.services.exteps.ioadapter.dfconsumer.DataCollector;
import com.ericsson.component.aia.services.exteps.ioadapter.dfconsumer.StringDataCollector;
import com.ericsson.component.aia.services.exteps.ioadapter.dfproducer.AvroDataProducer;
import com.ericsson.component.aia.services.exteps.ioadapter.dfproducer.BinaryDataProducer;
import com.ericsson.component.aia.services.exteps.ioadapter.dfproducer.DataProducer;
import com.ericsson.component.aia.services.exteps.ioadapter.dfproducer.StringDataProducer;

/**
 * A class to build the format specific handler classes
 *
 * @author eachsaj March 23, 2016
 *
 */
public final class MessagingService {
    // singleton instance
    private static MessagingService service;
    //Logger
    private static Logger logger = LoggerFactory.getLogger(MessagingService.class);
    // consumer services
    private ServiceLoader<DataCollector> consumerLoader;

    // consumer services
    private ServiceLoader<DataProducer> publisherLoader;

    private MessagingService() {
        logger.trace("creating Messaging service-->");
        consumerLoader = ServiceLoader.load(DataCollector.class);
        publisherLoader = ServiceLoader.load(DataProducer.class);
        logger.trace("creating Messaging service<--");
    }

    /**
     * return instance of {@link MessagingService}
     *
     */
    public static synchronized MessagingService getInstance() {
        if (service == null) {
            service = new MessagingService();
        }
        return service;
    }

    /**
     * create a {@link DataCollector} type instance based on the request , if the requested type is unknown, method will throw
     * {@link IllegalStateException}.
     *
     * @param type
     *            is the data format type
     * @return
     */
    public DataCollector getConsumerService(String type) {
        checkArgument(type != null, "Data format type  cannot be null.");
        logger.trace("getConsumerService({})-->", type);
        switch (type.toLowerCase()) {
            case IO_CONSTANTS.FORMAT_AVRO:
                final DataCollector proc = new AvroDataCollector();
                return proc;

            case IO_CONSTANTS.FORMAT_STRING:
                final StringDataCollector stringDataCollector = new StringDataCollector();
                return stringDataCollector;

            case IO_CONSTANTS.FORMAT_RAW:
                final BinaryDataCollector rawDataCollector = new BinaryDataCollector();
                return rawDataCollector;

            default:
                throw new IllegalArgumentException("Unidentified type requested.");
        }
    }

    public DataProducer getPublisherService(String type) {
        checkArgument(type != null, "Data format type  cannot be null.");
        switch (type) {
            case IO_CONSTANTS.FORMAT_AVRO:
                final DataProducer proc = new AvroDataProducer();
                return proc;

            case IO_CONSTANTS.FORMAT_STRING:
                final DataProducer stringDataCollector = new StringDataProducer();
                return stringDataCollector;

            case IO_CONSTANTS.FORMAT_RAW:
                final DataProducer rawDataCollector = new BinaryDataProducer();
                return rawDataCollector;

            default:
                throw new IllegalArgumentException("Unidentified type requested.");
        }

    }

    /**
     * A method create listener for Avro data format
     *
     * @param eventHandlerContext
     *            instance of {@link EventHandlerContext}
     * @param props
     *            connection properties
     */

    /*public static DataCollector createDataCollector(MessageServiceTypes uRI, EventHandlerContext eventHandlerContext, Properties props) {
        checkArgument(eventHandlerContext != null, "EventHandlerContext cannot be null.");
        checkArgument(props != null, "Properties cannot be null.");
        final String type = props.getProperty(EPS_FORMAT_DATA, FORMAT_AVRO).toLowerCase();
        checkArgument(type != null, "type requested  cannot be null.");
        switch (type.toLowerCase()) {
            case FORMAT_AVRO:
                DataCollector proc = new AvroDataCollector();
                proc.create(uRI, eventHandlerContext, props);
                return proc;

            case FORMAT_STRING:
                StringDataCollector stringDataCollector = new StringDataCollector();
                stringDataCollector.create(uRI, eventHandlerContext, props);
                return stringDataCollector;

            case FORMAT_RAW:
                RawDataCollector rawDataCollector = new RawDataCollector();
                rawDataCollector.create(uRI, eventHandlerContext, props);
                return rawDataCollector;

            default:
                throw new IllegalArgumentException("Unidentified type requested.");
        }

    }
*/
    /**
     * A method create data producer for the data format type requested
     *
     * @param eventHandlerContext
     *            instance of {@link EventHandlerContext}
     * @param props
     *            connection properties
     */
    /*public static DataProducer createDataProducer(MessageServiceTypes uRI, EventHandlerContext eventHandlerContext, Properties props) {
        checkArgument(eventHandlerContext != null, "EventHandlerContext cannot be null.");
        checkArgument(props != null, "Properties cannot be null.");
        final String type = props.getProperty(IO_CONSTANTS.EPS_FORMAT_DATA, IO_CONSTANTS.FORMAT_AVRO).toLowerCase();
        checkArgument(type != null, "type requested  cannot be null.");
        switch (type) {
            case FORMAT_AVRO:
                DataProducer proc = new AvroDataProducer();
                proc.create(uRI, eventHandlerContext, props);
                return proc;

            case FORMAT_STRING:
                DataProducer stringDataCollector = new StringDataProducer();
                stringDataCollector.create(uRI, eventHandlerContext, props);
                return stringDataCollector;

            case FORMAT_RAW:
                DataProducer rawDataCollector = new RawDataProducer();
                rawDataCollector.create(uRI, eventHandlerContext, props);
                return rawDataCollector;

            default:
                throw new IllegalArgumentException("Unidentified type requested.");
        }

    }*/
}
