/**
 * 
 * (C) Copyright LM Ericsson System Expertise AT/LMI, 2016
 *
 * The copyright to the computer program(s) herein is the property of Ericsson  System Expertise EEI, Sweden.
 * The program(s) may be used and/or copied only with the written permission from Ericsson System Expertise
 * AT/LMI or in  * accordance with the terms and conditions stipulated in the agreement/contract under which
 * the program(s) have been supplied.
 *
 */

package test.integration;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;

/**
 * @author eachsaj
 * May 5, 2016
 */
public class StringPartitionerX implements Partitioner {
	
	private List<PartitionInfo> availablePartitionsForTopic;

	/* (non-Javadoc)
	 * @see org.apache.kafka.common.Configurable#configure(java.util.Map)
	 */
	@Override
	public void configure(Map<String, ?> configs) {
		// TODO Auto-generated method stub
		
	}
	
	/* (non-Javadoc)
	 * @see org.apache.kafka.clients.producer.Partitioner#partition(java.lang.String, java.lang.Object, byte[], java.lang.Object, byte[], org.apache.kafka.common.Cluster)
	 */
	@Override
	public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
		if(availablePartitionsForTopic==null){
			availablePartitionsForTopic = cluster.availablePartitionsForTopic(topic);
		}
		String keyreported=null;
		if(key!=null){
			 keyreported = (String) key;
		}else{
			 keyreported= new String((byte[])value);
		}
		if(availablePartitionsForTopic==null){
			return 0;
		}
		int pKey = availablePartitionsForTopic.size()==0?0:toPositive(keyreported.hashCode())%availablePartitionsForTopic.size();
		System.out.println("Partition key "+ pKey);
		return pKey;
	}
    /**
     * A cheap way to deterministically convert a number to a positive value. When the input is
     * positive, the original value is returned. When the input number is negative, the returned
     * positive value is the original value bit AND against 0x7fffffff which is not its absolutely
     * value.
     *
     * Note: changing this method in the future will possibly cause partition selection not to be
     * compatible with the existing messages already placed on a partition.
     *
     * @param number a given number
     * @return a positive number.
     */
    private static int toPositive(int number) {
        return number & 0x7fffffff;
    }
	
	/* (non-Javadoc)
	 * @see org.apache.kafka.clients.producer.Partitioner#close()
	 */
	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}
	
}
