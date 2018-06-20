package io.jenkins.plugins.remotingkafka;

import io.jenkins.plugins.remotingkafka.builder.KafkaTransportBuilder;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Listens to incoming message from Kafka and produce an outgoing response message.
 */
public final class KafkaClientListener implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(KafkaClientListener.class.getName());
    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private final String inMessage;
    private final String outMessage;
    private final String producerTopic;
    private final String producerKey;
    private final String consumerTopic;
    private final String consumerKey;
    private final int producerPartition;
    private final int consumerPartition;
    private Producer<String, byte[]> producer;
    private Consumer<String, byte[]> consumer;
    private volatile boolean shuttingDown;

    public KafkaClientListener(String inMessage, String outMessage, KafkaTransportBuilder settings) {
        this.inMessage = inMessage;
        this.outMessage = outMessage;
        this.producerTopic = settings.getProducerTopic();
        this.producerKey = settings.getProducerKey();
        this.consumerKey = settings.getConsumerKey();
        this.consumerTopic = settings.getConsumerTopic();
        this.producerPartition = settings.getProducerPartition();
        this.consumerPartition = settings.getConsumerPartition();
        this.producer = settings.getProducer();
        this.consumer = settings.getConsumer();
    }

    @Override
    public void run() {
        LOGGER.info("Kafka client listener is running...");
        while (!shuttingDown) {
            TopicPartition partition = new TopicPartition(consumerTopic, consumerPartition);
            consumer.assign(Arrays.asList(partition));
            while (true) {
                // Wait from master.
                ConsumerRecords<String, byte[]> records = consumer.poll(0);
                consumer.commitSync();
                String receivedMessage = "";
                for (ConsumerRecord<String, byte[]> record : records) {
                    if (record.key().equals(consumerKey)) {
                        receivedMessage = new String(record.value(), UTF_8);
                        LOGGER.info("Received an in message=" + receivedMessage);
                    }
                }

                // Send to master.
                if (receivedMessage.equals(inMessage)) {
                    producer.send(new ProducerRecord<>(producerTopic, producerPartition, producerKey, outMessage.getBytes(UTF_8)));
                    LOGGER.info("Sent an out message=" + outMessage);
                    break;
                }
            }
        }
        if (producer != null) {
            producer.close();
        }
        if (consumer != null) {
            consumer.close();
        }
    }
}
