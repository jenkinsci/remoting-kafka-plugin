package io.jenkins.plugins.remotingkafka.commandtransport;

import hudson.remoting.AbstractSynchronousByteArrayCommandTransport;
import hudson.remoting.Capability;
import hudson.remoting.Channel;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Referenced from ChunkedCommandTransport.
 */
public class KafkaChunkedCommandTransport extends AbstractSynchronousByteArrayCommandTransport {
    private static final Logger LOGGER = Logger.getLogger(KafkaClassicCommandTransport.class.getName());
    private final Capability remoteCapability;
    // We use a single instance producer/consumer for each command transport for now.
    private final Producer<String, byte[]> producer;
    private final Consumer<String, byte[]> consumer;
    private final String producerTopic;
    private final String producerKey;
    private final List<String> consumerTopics;
    private final String consumerKey;
    private final long pollTimeout;

    public KafkaChunkedCommandTransport(Capability remoteCapability, String producerTopic, String producerKey
            , List<String> consumerTopics, String consumerKey, long pollTimeout
            , Producer<String, byte[]> producer, Consumer<String, byte[]> consumer) {
        this.remoteCapability = remoteCapability;
        this.producerKey = producerKey;
        this.producerTopic = producerTopic;
        this.consumerKey = consumerKey;
        this.consumerTopics = consumerTopics;
        this.producer = producer;
        this.consumer = consumer;
        this.pollTimeout = pollTimeout;
    }

    @Override
    public byte[] readBlock(Channel channel) throws IOException, ClassNotFoundException {
        consumer.subscribe(consumerTopics);
        while (true) {
            byte[] data = null;
            consumer.subscribe(consumerTopics);
            while (true) {
                ConsumerRecords<String, byte[]> records = consumer.poll(pollTimeout);
                for (ConsumerRecord<String, byte[]> record : records) {
                    if (record.key().equals(consumerKey)) {
                        data = record.value();
                    }
                }
                if (data != null) return data;
            }
        }
    }

    @Override
    public void writeBlock(Channel channel, byte[] bytes) throws IOException {
        producer.send(new ProducerRecord<String, byte[]>(producerTopic, producerKey, bytes));
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
