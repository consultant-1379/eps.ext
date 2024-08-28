package com.ericsson.component.aia.services.exteps.ioadapter.kafka;

import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;

class KafkaConsumerListener implements Runnable {

    private final KafkaStream<byte[], byte[]> stream;

    private final KafkaInputAdapter adapter;

    KafkaConsumerListener(final KafkaStream<byte[], byte[]> stream, final KafkaInputAdapter adapter) {
        if (stream == null) {
            throw new IllegalArgumentException("Stream must not be null");
        }
        if (adapter == null) {
            throw new IllegalArgumentException("Adapter must not be null");
        }
        this.stream = stream;
        this.adapter = adapter;
    }

    @Override
    public void run() {
        final ConsumerIterator<byte[], byte[]> it = stream.iterator();
        while (it.hasNext()) {
            final byte[] message = it.next().message();
            this.adapter.sendEvent(message);
        }
    }

}
