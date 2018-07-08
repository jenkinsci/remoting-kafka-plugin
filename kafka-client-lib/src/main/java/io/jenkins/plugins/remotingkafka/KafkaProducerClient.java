package io.jenkins.plugins.remotingkafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KafkaProducerClient {
    private static final Logger LOGGER = Logger.getLogger(KafkaProducerClient.class.getName());
    private static volatile KafkaProducerClient instance;
    private volatile Producer<String, byte[]> byteProducer;

    private KafkaProducerClient() {

    }

    public static KafkaProducerClient getInstance() {
        if (instance == null) {
            synchronized (KafkaProducerClient.class) {
                if (instance == null) {
                    instance = new KafkaProducerClient();
                }
            }
        }
        return instance;
    }

    public Producer<String, byte[]> getByteProducer(Properties producerProps) {
        if (byteProducer == null) {
            synchronized (KafkaProducerClient.class) {
                if (byteProducer == null) {
                    ClassLoader cl = Thread.currentThread().getContextClassLoader();
                    try {
                        // Kafka uses reflection for loading authentication settings, use its classloader.
                        Thread.currentThread().setContextClassLoader(
                                org.apache.kafka.clients.producer.KafkaProducer.class.getClassLoader());
                        byteProducer = new KafkaProducer<>(producerProps);
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Exception when creating a Kafka producer", e);
                    } finally {
                        Thread.currentThread().setContextClassLoader(cl);
                    }
                }
            }
        }
        return byteProducer;
    }
}
