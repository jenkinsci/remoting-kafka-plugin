package io.jenkins.plugins.remotingkafka.commandtransport;

import hudson.remoting.Capability;
import hudson.remoting.Command;
import hudson.remoting.SynchronousCommandTransport;
import io.jenkins.plugins.remotingkafka.KafkaUtils;
import io.jenkins.plugins.remotingkafka.builder.KafkaTransportBuilder;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;

import java.io.IOException;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Referenced from ClassicCommandTransport.
 */
public class KafkaClassicCommandTransport extends SynchronousCommandTransport {
    private static final Logger LOGGER = Logger.getLogger(KafkaClassicCommandTransport.class.getName());
    private final Capability remoteCapability;

    private final Producer<String, byte[]> producer;
    private final Consumer<String, byte[]> consumer;
    private final String producerTopic;
    private final String producerKey;
    private final String consumerTopic;
    private final String consumerKey;
    private final long pollTimeout;
    private final int producerPartition;
    private final int consumerPartition;
    private boolean isReadClosed;

    private Queue<Command> commandQueue;

    public KafkaClassicCommandTransport(KafkaTransportBuilder settings) {
        this.remoteCapability = settings.getRemoteCapability();
        this.producerKey = settings.getProducerKey();
        this.producerTopic = settings.getProducerTopic();
        this.consumerKey = settings.getConsumerKey();
        this.consumerTopic = settings.getConsumerTopic();
        this.producer = settings.getProducer();
        this.consumer = settings.getConsumer();
        this.pollTimeout = settings.getPollTimeout();
        this.producerPartition = settings.getProducerPartition();
        this.consumerPartition = settings.getConsumerPartition();
        this.commandQueue = new ConcurrentLinkedQueue<>();
        this.isReadClosed = false;
    }

    @Override
    public final Capability getRemoteCapability() throws IOException {
        return remoteCapability;
    }

    @Override
    public final void write(Command cmd, boolean last) throws IOException {
        byte[] bytes = SerializationUtils.serialize(cmd);
        producer.send(new ProducerRecord<>(producerTopic, producerPartition, producerKey, bytes));
        LOGGER.log(Level.FINE, "Sent a command=" + cmd.toString() + ", in topic=" + producerTopic + ", with key=" + producerKey);
    }

    @Override
    public final void closeWrite() throws IOException {
        producer.close();
    }

    @Override
    public final void closeRead() throws IOException {
        if (!isReadClosed) {
            consumer.commitSync();
            KafkaUtils.unassignConsumer(consumer);
            isReadClosed = true;
        }
    }

    @Override
    public final Command read() throws IOException, ClassNotFoundException, InterruptedException {
        if (!commandQueue.isEmpty()) {
            Command cmd = commandQueue.poll();
            LOGGER.log(Level.FINE, "Received a command: " + cmd.toString());
            return cmd;
        }
        TopicPartition partition = new TopicPartition(consumerTopic, consumerPartition);
        consumer.assign(Arrays.asList(partition));
        while (true) { // Poll consumer until we get something
            ConsumerRecords<String, byte[]> records = consumer.poll(pollTimeout);
            Command cmd = null;
            for (ConsumerRecord<String, byte[]> record : records) {
                if (record.key().equals(consumerKey)) {
                    Command read = Command.readFrom(channel, record.value());
                    if (cmd == null) { // first one goes to the immediate execution
                        cmd = read;
                    } else { // write the rest to the queue
                        commandQueue.add(read);
                    }
                }
            }
            if (cmd != null) {
                consumer.commitSync();
                LOGGER.log(Level.FINE, "Received a command: " + cmd.toString());
                return cmd;
            }
        }
    }
}
