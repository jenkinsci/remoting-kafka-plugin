package io.jenkins.plugins.remotingkafka;

import io.jenkins.plugins.remotingkafka.builder.ConsumerPropertiesBuilder;
import io.jenkins.plugins.remotingkafka.builder.ProducerPropertiesBuilder;
import io.jenkins.plugins.remotingkafka.enums.AutoOffsetReset;
import io.jenkins.plugins.remotingkafka.enums.ProducerAcks;
import io.jenkins.plugins.remotingkafka.exception.RemotingKafkaConfigurationException;
import kafka.admin.AdminUtils;
import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KafkaUtils {
    private static final Logger LOGGER = Logger.getLogger(KafkaUtils.class.getName());

    public static Producer<String, byte[]> createByteProducer(String kafkaURL, @Nullable Properties securityProps)
            throws RemotingKafkaConfigurationException {
        Properties producerProps = new ProducerPropertiesBuilder()
                .withBoostrapServers(kafkaURL)
                .withAcks(ProducerAcks.ALL)
                .withKeySerializer(StringSerializer.class)
                .withValueSerialier(ByteArraySerializer.class)
                .withSecurityProps(securityProps)
                .build();
        // As producer props may get change, it's better to not reuse producer instance for now.
        //        Producer<String, byte[]> producer = KafkaProducerClient.getInstance().getByteProducer(producerProps);
        Producer<String, byte[]> producer = null;
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            // Kafka uses reflection for loading authentication settings, use its classloader.
            Thread.currentThread().setContextClassLoader(
                    org.apache.kafka.clients.producer.KafkaProducer.class.getClassLoader());
            producer = new KafkaProducer<>(producerProps);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception when creating a Kafka producer", e);
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
        return producer;
    }

    public static KafkaConsumer<String, byte[]> createByteConsumer(String kafkaURL, String consumerGroupID, @Nullable Properties securityProps)
            throws RemotingKafkaConfigurationException {
        Properties consumerProps = new ConsumerPropertiesBuilder()
                .withBootstrapServers(kafkaURL)
                .withGroupID(consumerGroupID)
                .withEnableAutoCommit(false)
                .withAutoOffsetReset(AutoOffsetReset.EARLIEST)
                .withKeyDeserializer(StringDeserializer.class)
                .withValueDeserializer(ByteArrayDeserializer.class)
                .withSecurityProps(securityProps)
                .build();
        KafkaConsumer<String, byte[]> consumer = null;
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            // Kafka uses reflection for loading authentication settings, use its classloader.
            Thread.currentThread().setContextClassLoader(
                    org.apache.kafka.clients.consumer.KafkaConsumer.class.getClassLoader());
            consumer = new KafkaConsumer<>(consumerProps);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception when creating a Kafka consumer", e);
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
        return consumer;
    }

    public static void unassignConsumer(Consumer<String, byte[]> consumer) {
        consumer.assign(new ArrayList<>());
        consumer.close();
    }

    public static void createTopic(String topic, String zookeeperHost, int noOfPartitions, int noOfReplication) {
        ZkClient zkClient = null;
        ZkUtils zkUtils;
        try {
            int sessionTimeOutInMs = 15 * 1000; // 15 secs
            int connectionTimeOutInMs = 10 * 1000; // 10 secs

            zkClient = new ZkClient(zookeeperHost, sessionTimeOutInMs, connectionTimeOutInMs, ZKStringSerializer$.MODULE$);
            zkUtils = new ZkUtils(zkClient, new ZkConnection(zookeeperHost), false);

            Properties topicConfiguration = new Properties();
            AdminUtils.createTopic(zkUtils, topic, noOfPartitions, noOfReplication, topicConfiguration, null);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (zkClient != null) {
                zkClient.close();
            }
        }
    }
}
