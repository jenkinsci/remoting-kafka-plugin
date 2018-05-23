package io.jenkins.plugins.remotingkafka.commandtransport;

import hudson.remoting.AbstractByteBufferCommandTransport;
import hudson.remoting.Capability;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

/**
 * Referenced from ChannelApplicationLayer.ByteBufferCommandTransport
 */
public class KafkaByteBufferCommandTransport extends AbstractByteBufferCommandTransport {
    private static final Logger LOGGER = Logger.getLogger(KafkaByteBufferCommandTransport.class.getName());
    private final Capability remoteCapability;

    // We use a single instance producer/consumer for each command transport for now.
    private final Producer<String, ByteBuffer> producer;
    private final Consumer<String, ByteBuffer> consumer;

    private final String topic;
    private final String key;

    public KafkaByteBufferCommandTransport(Capability remoteCapability, String topic, String key
            , Producer<String, ByteBuffer> producer, Consumer<String, ByteBuffer> consumer) {
        this.remoteCapability = remoteCapability;
        this.producer = producer;
        this.consumer = consumer;
        this.topic = topic;
        this.key = key;
    }

    @Override
    protected void write(ByteBuffer header, ByteBuffer data) throws IOException {
        producer.send(new ProducerRecord<String, ByteBuffer>(topic, key, header));
        producer.send(new ProducerRecord<String, ByteBuffer>(topic, key, data));
    }

    @Override
    public Capability getRemoteCapability() throws IOException {
        return remoteCapability;
    }

    @Override
    public void closeWrite() throws IOException {
        // Because Kafka producer is thread safe, we do not need to close the producer and may reuse.
//        producer.close();
    }

    @Override
    public void closeRead() throws IOException {
        consumer.commitSync();
        consumer.close();
    }
}
