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

    private KafkaConsumerClient() {

    }

    public synchronized static KafkaConsumerClient getInstance() {
        if (instance == null) {
            instance = new KafkaConsumerClient();
        }
        return instance;
    }

    public String subscribe(Properties props, List<String> topics, long timeout) {
        if (consumer == null) {
            consumer = new KafkaConsumer<>(props);
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
}
