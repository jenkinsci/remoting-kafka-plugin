package io.jenkins.plugins.remotingkafka.commandtransport;

import hudson.remoting.AbstractByteArrayCommandTransport;
import hudson.remoting.Capability;
import hudson.remoting.Channel;
import hudson.remoting.ChunkHeader;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Referenced from NioChannelHub.NioTransport
 */
public class KafkaByteArrayCommandTransport extends AbstractByteArrayCommandTransport {
    public static final Logger LOGGER = Logger.getLogger(KafkaByteArrayCommandTransport.class.getName());
    private static final int DEFAULT_TRANSPORT_FRAME_SIZE = 8192;

    private final Capability remoteCapability;
    // We use a single instance producer/consumer for each command transport for now.
    private final Producer<String, byte[]> producer;
    private final Consumer<String, byte[]> consumer;
    private final String topic;
    private final String key;
    private ByteArrayReceiver receiver;
    private int transportFrameSize;

    public KafkaByteArrayCommandTransport(Capability remoteCapability, String topic, String key
            , Producer<String, byte[]> producer, Consumer<String, byte[]> consumer) {
        this.remoteCapability = remoteCapability;
        this.topic = topic;
        this.key = key;
        this.producer = producer;
        this.consumer = consumer;
        this.transportFrameSize = DEFAULT_TRANSPORT_FRAME_SIZE;
    }

    @Override
    public void writeBlock(Channel channel, byte[] bytes) throws IOException {
        boolean hasMore;
        int pos = 0;
        do {
            int frame = Math.min(transportFrameSize, bytes.length - pos); // # of bytes we send in this chunk
            hasMore = frame + pos < bytes.length;
            byte[] chunkHeader = ChunkHeader.pack(frame, hasMore);
            producer.send(new ProducerRecord<String, byte[]>(topic, key, chunkHeader));
            byte[] payload = Arrays.copyOfRange(bytes, pos, pos + frame);
            producer.send(new ProducerRecord<String, byte[]>(topic, key, payload));
            pos += frame;
        } while (hasMore);
    }

    @Override
    public void setup(@Nonnull ByteArrayReceiver byteArrayReceiver) {
        this.receiver = byteArrayReceiver;
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
