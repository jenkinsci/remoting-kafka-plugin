package io.jenkins.plugins.remotingkafka;

import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Pooling mechanism to reuse Kafka consumers.
 */
public class KafkaConsumerPool {
    private static final Logger LOGGER = Logger.getLogger(KafkaConsumerPool.class.getName());
    private static final Object instanceLock = new Object();
    private static final Object poolLock = new Object();
    private static volatile KafkaConsumerPool instance;
    private LinkedBlockingQueue<KafkaConsumer<String, byte[]>> byteConsumerPool;
    private Properties byteConsumerProps;

    private KafkaConsumerPool() {
        byteConsumerPool = new LinkedBlockingQueue<>();
    }

    public static KafkaConsumerPool getInstance() {
        if (instance == null) {
            synchronized (instanceLock) {
                if (instance == null) {
                    instance = new KafkaConsumerPool();
                }
            }
        }
        return instance;
    }

    public void init(int poolSize, Properties byteConsumerProps) {
        if (byteConsumerPool.isEmpty()) {
            this.byteConsumerProps = byteConsumerProps;
            for (int i = 0; i < poolSize; i++) {
                KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(byteConsumerProps);
                byteConsumerPool.add(consumer);
            }
        }
    }

    public KafkaConsumer<String, byte[]> getbyteConsumer() {
        synchronized (poolLock) {
            while (byteConsumerPool.isEmpty()) {
                try {
                    poolLock.wait();
                } catch (InterruptedException e) {
                    LOGGER.log(Level.SEVERE, "InterruptedException while getting a byte consumer", e);
                }
            }
            return byteConsumerPool.poll();
        }
    }

    public void releasebyteConsumer() {
        synchronized (poolLock) {
            KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(byteConsumerProps);
            byteConsumerPool.add(consumer);
        }
    }
}
