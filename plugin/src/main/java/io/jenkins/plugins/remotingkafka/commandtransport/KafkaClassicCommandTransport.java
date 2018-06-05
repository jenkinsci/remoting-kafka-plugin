package io.jenkins.plugins.remotingkafka.commandtransport;

import hudson.remoting.Capability;
import hudson.remoting.Command;
import hudson.remoting.SynchronousCommandTransport;
import io.jenkins.plugins.remotingkafka.KafkaConsumerPool;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Referenced from ClassicCommandTransport.
 */
public class KafkaClassicCommandTransport extends SynchronousCommandTransport {

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

    public KafkaClassicCommandTransport(Capability remoteCapability, String producerTopic, String producerKey
            , List<String> consumerTopics, String consumerKey, long pollTimeout
            , Producer<String, byte[]> producer, KafkaConsumer<String, byte[]> consumer) {
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
    public final Capability getRemoteCapability() throws IOException {
        return remoteCapability;
    }

    @Override
    public final void write(Command cmd, boolean last) throws IOException {
        byte[] bytes = SerializationUtils.serialize(cmd);
        producer.send(new ProducerRecord<>(producerTopic, producerKey, bytes));
        LOGGER.info("Sent a command=" + cmd.toString() + ", in topic=" + producerTopic + ", with key=" + producerKey);
    }

    @Override
    public final void closeWrite() throws IOException {
        // Because Kafka producer is thread safe, we do not need to close the producer and may reuse.
//        producer.close();
    }

    @Override
    public final void closeRead() throws IOException {
        consumer.commitSync();
        consumer.close();
        KafkaConsumerPool.getInstance().releasebyteConsumer();
    }

    @Override
    public final Command read() throws IOException, ClassNotFoundException, InterruptedException {
        Command cmd = null;
        consumer.subscribe(consumerTopics);
        while (true) {
            ConsumerRecords<String, byte[]> records = consumer.poll(pollTimeout);
            for (ConsumerRecord<String, byte[]> record : records) {
                if (record.key().equals(consumerKey)) {
                    cmd = (Command) SerializationUtils.deserialize(record.value());
                }
            }
            if (cmd != null) {
                LOGGER.info("Received a command: " + cmd.toString());
                return cmd;
            }
        }
    }
}
