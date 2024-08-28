/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.exteps.io.adapter.partition;

import org.apache.avro.generic.GenericRecord;

public class GenericRecordWithPartitionNumber {

    GenericRecord genericRecord;
    Integer partitionNumber;

    public GenericRecordWithPartitionNumber(final GenericRecord genericRecord, final Integer partitionNumber) {
        this.genericRecord = genericRecord;
        this.partitionNumber = partitionNumber;
    }

    /**
     * @return the genericRecord
     */
    public GenericRecord getGenericRecord() {
        return genericRecord;
    }

    /**
     * @param genericRecord
     *            the genericRecord to set
     */
    public void setGenericRecord(final GenericRecord genericRecord) {
        this.genericRecord = genericRecord;
    }

    /**
     * @return the partitionNumber
     */
    public Integer getPartitionNumber() {
        return partitionNumber;
    }

    /**
     * @param partitionNumber
     *            the partitionNumber to set
     */
    public void setPartitionNumber(final Integer partitionNumber) {
        this.partitionNumber = partitionNumber;
    }

}
