package io.jenkins.plugins.remotingkafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KafkaProducerClient {
    private static final Logger LOGGER = Logger.getLogger(KafkaProducerClient.class.getName());
    private static KafkaProducerClient instance;
    private Producer<String, String> producer;
    private Properties props;

    private KafkaProducerClient() {

    }

    public synchronized static KafkaProducerClient getInstance() {
        if (instance == null) {
            instance = new KafkaProducerClient();
        }
        return instance;
    }

    private void initProducer(String connectionURL) {
        this.setProps(connectionURL);
        Thread.currentThread().setContextClassLoader(null);
        producer = new KafkaProducer<String, String>(props);
    }

    private void setProps(String connectionURL) {
        props = new Properties();
        props.put("bootstrap.servers", connectionURL);
        props.put("acks", "all");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    }

    public void send(String connectionURL, String topic, String key, String value) {
        if (producer == null) {
            initProducer(connectionURL);
        }
        try {
            producer.send(new ProducerRecord<String, String>(topic, key, value)).get();
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Interrupted Exception", e);
        } catch (ExecutionException e) {
            LOGGER.log(Level.SEVERE, "Execution Exception", e);
        }
        LOGGER.info("Sent a record to topic=" + topic + ", key=" + key + ", value=" + value);
    }
}
