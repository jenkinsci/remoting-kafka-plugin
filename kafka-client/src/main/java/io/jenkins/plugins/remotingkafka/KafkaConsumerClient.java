package io.jenkins.plugins.remotingkafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

public class KafkaConsumerClient {
    private static final Logger LOGGER = Logger.getLogger(KafkaConsumerClient.class.getName());
    private static KafkaConsumerClient instance;
    private KafkaConsumer<String, String> consumer;
    private Properties props;

    private KafkaConsumerClient() {

    }

    public synchronized static KafkaConsumerClient getInstance() {
        if (instance == null) {
            instance = new KafkaConsumerClient();
        }
        return instance;
    }

    public String subscribe(String connectionURL, List<String> topics, long timeout) {
        if (consumer == null) {
            initConsumer(connectionURL);
        }
        consumer.subscribe(topics);
        StringBuffer topicList = new StringBuffer();
        for (String topic : topics) {
            topicList.append(topic + ", ");
        }
        LOGGER.info("Subscribed to topics: " + topicList.toString());
        ConsumerRecords<String, String> records = consumer.poll(timeout);
        while (records.isEmpty()) {
            records = consumer.poll(timeout);
        }
        String payload = "";
        for (ConsumerRecord<String, String> record : records) {
            payload = record.value();
            LOGGER.info("Consumed a record with offset=" + record.offset() + ", key=" + record.key()
                    + ", value=" + record.value());
        }
        consumer.commitSync();
        return payload;
    }

    private void initConsumer(String connectionURL) {
        this.setProps(connectionURL);
        Thread.currentThread().setContextClassLoader(null);
        consumer = new KafkaConsumer<>(props);
    }

    public Properties getProps() {
        return props;
    }

    private void setProps(String connectionURL) {
        props = new Properties();
        props.put("bootstrap.servers", connectionURL);
        props.put("group.id", "agent");
        props.put("enable.auto.commit", "false");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        LOGGER.info("Finished setting up Kafka configuration with connectionURL: " + connectionURL);
    }
}
