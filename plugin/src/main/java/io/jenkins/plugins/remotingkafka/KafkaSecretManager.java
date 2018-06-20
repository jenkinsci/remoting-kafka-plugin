package io.jenkins.plugins.remotingkafka;

import io.jenkins.plugins.remotingkafka.builder.KafkaTransportBuilder;
import io.jenkins.plugins.remotingkafka.exception.RemotingKafkaConfigurationException;
import jenkins.security.HMACConfidentialKey;
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
 * Manage secret logic.
 */
public final class KafkaSecretManager {
    private static final Logger LOGGER = Logger.getLogger(KafkaSecretManager.class.getName());
    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private static final HMACConfidentialKey AGENT_SECRET =
            new HMACConfidentialKey(KafkaSecretManager.class, "secret");
    private final String agentName;
    private final String producerTopic;
    private final String producerKey;
    private final String consumerTopic;
    private final String consumerKey;
    private final int producerPartition;
    private final int consumerPartition;
    private Producer<String, byte[]> producer;
    private Consumer<String, byte[]> consumer;

    public KafkaSecretManager(String agentName, KafkaTransportBuilder settings) {
        this.agentName = agentName;
        this.producer = settings.getProducer();
        this.consumer = settings.getConsumer();
        this.producerTopic = settings.getProducerTopic();
        this.producerKey = settings.getProducerKey();
        this.producerPartition = settings.getProducerPartition();
        this.consumerTopic = settings.getConsumerTopic();
        this.consumerKey = settings.getConsumerKey();
        this.consumerPartition = settings.getConsumerPartition();
    }

    public static String getConnectionSecret(String agentName) {
        return AGENT_SECRET.mac(agentName);
    }

    public void waitForValidAgent() throws RemotingKafkaConfigurationException {
        initHandshake();
        waitForSecret();
    }

    private void waitForSecret() {
        String connectionSecret = getConnectionSecret(agentName);
        String agentSecret = "";
        TopicPartition partition = new TopicPartition(consumerTopic, consumerPartition);
        consumer.assign(Arrays.asList(partition));
        while (true) {
            ConsumerRecords<String, byte[]> records = consumer.poll(0);
            consumer.commitSync();
            for (ConsumerRecord<String, byte[]> record : records) {
                String receivedValue = new String(record.value(), UTF_8);
                LOGGER.info("Received a secret=" + receivedValue);
                if (record.key().equals(consumerKey)) {
                    agentSecret = receivedValue;
                    if (connectionSecret.equals(agentSecret)) {
                        consumer.close();
                        return;
                    } else {
                        LOGGER.warning("Please sent a valid secret");
                    }
                } else {
                    LOGGER.warning("Please sent a valid secret");
                }
            }
        }
    }

    private void initHandshake() throws RemotingKafkaConfigurationException {
        String msg = "hello";
        producer.send(new ProducerRecord<>(producerTopic, producerPartition, producerKey, msg.getBytes(UTF_8)));
        LOGGER.info("Init secret exchange by sending msg=" + msg + ", in topic=" + producerTopic + ", with partition="
                + producerPartition + ", with key=" + producerKey);
    }
}
