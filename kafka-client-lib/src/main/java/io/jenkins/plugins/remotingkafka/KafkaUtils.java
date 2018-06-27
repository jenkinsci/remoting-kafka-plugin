package io.jenkins.plugins.remotingkafka;

import io.jenkins.plugins.remotingkafka.builder.AutoOffsetReset;
import io.jenkins.plugins.remotingkafka.builder.ConsumerPropertiesBuilder;
import io.jenkins.plugins.remotingkafka.builder.ProducerAcks;
import io.jenkins.plugins.remotingkafka.builder.ProducerPropertiesBuilder;
import io.jenkins.plugins.remotingkafka.exception.RemotingKafkaConfigurationException;
import kafka.admin.AdminUtils;
import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.ArrayList;
import java.util.Properties;

public class KafkaUtils {
    public static Producer<String, byte[]> createByteProducer(String kafkaURL) throws RemotingKafkaConfigurationException {
        Properties producerProps = new ProducerPropertiesBuilder()
                .withBoostrapServers(kafkaURL)
                .withAcks(ProducerAcks.ALL)
                .withKeySerializer(StringSerializer.class)
                .withValueSerialier(ByteArraySerializer.class)
                .build();
        Producer<String, byte[]> producer = KafkaProducerClient.getInstance().getByteProducer(producerProps);
        return producer;
    }

    public static KafkaConsumer<String, byte[]> createByteConsumer(String kafkaURL, String consumerGroupID) throws RemotingKafkaConfigurationException {
        Properties consumerProps = new ConsumerPropertiesBuilder()
                .withBootstrapServers(kafkaURL)
                .withGroupID(consumerGroupID)
                .withEnableAutoCommit(false)
                .withAutoOffsetReset(AutoOffsetReset.EARLIEST)
                .withKeyDeserializer(StringDeserializer.class)
                .withValueDeserializer(ByteArrayDeserializer.class)
                .build();
        KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(consumerProps);
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
