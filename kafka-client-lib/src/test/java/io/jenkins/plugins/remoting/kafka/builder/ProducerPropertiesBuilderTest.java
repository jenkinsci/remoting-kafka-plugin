package io.jenkins.plugins.remoting.kafka.builder;


import io.jenkins.plugins.remotingkafka.KafkaConfigs;
import io.jenkins.plugins.remotingkafka.builder.ProducerPropertiesBuilder;
import io.jenkins.plugins.remotingkafka.enums.ProducerAcks;
import io.jenkins.plugins.remotingkafka.exception.RemotingKafkaConfigurationException;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class ProducerPropertiesBuilderTest {
    @Test
    public void testBuildAllConfigs() throws RemotingKafkaConfigurationException {
        Properties props = new ProducerPropertiesBuilder()
                .withBoostrapServers("localhost:9092")
                .withAcks(ProducerAcks.ALL)
                .withKeySerializer(StringSerializer.class)
                .withValueSerialier(StringSerializer.class)
                .build();
        assertEquals("localhost:9092", props.get(KafkaConfigs.BOOTSTRAP_SERVERS));
        assertEquals(ProducerAcks.ALL.toString(), props.get(KafkaConfigs.ACKS));
        assertEquals(StringSerializer.class, props.get(KafkaConfigs.KEY_SERIALIZER));
        assertEquals(StringSerializer.class, props.get(KafkaConfigs.VALUE_SERIALIZER));
    }

    @Test
    public void testBuildNoAcks() throws RemotingKafkaConfigurationException {
        Properties props = new ProducerPropertiesBuilder()
                .withBoostrapServers("localhost:9092")
                .withKeySerializer(StringSerializer.class)
                .withValueSerialier(StringSerializer.class)
                .build();
        assertEquals("localhost:9092", props.get(KafkaConfigs.BOOTSTRAP_SERVERS));
        assertEquals(null, props.get(KafkaConfigs.ACKS));
        assertEquals(StringSerializer.class, props.get(KafkaConfigs.KEY_SERIALIZER));
        assertEquals(StringSerializer.class, props.get(KafkaConfigs.VALUE_SERIALIZER));
    }

    @Test
    public void testBuildNoBootstrapServers() {
        try {
            Properties props = new ProducerPropertiesBuilder()
                    .withAcks(ProducerAcks.ALL)
                    .withKeySerializer(StringSerializer.class)
                    .withValueSerialier(StringSerializer.class)
                    .build();
        } catch (Exception e) {
            assertEquals(RemotingKafkaConfigurationException.class, e.getClass());
            assertEquals("Please provide Kafka producer bootstrap servers", e.getMessage());
        }
    }

    @Test
    public void testBuildNoKeySerializer() {
        try {
            Properties props = new ProducerPropertiesBuilder()
                    .withBoostrapServers("localhost:9092")
                    .withAcks(ProducerAcks.ALL)
                    .withValueSerialier(StringSerializer.class)
                    .build();
        } catch (Exception e) {
            assertEquals(RemotingKafkaConfigurationException.class, e.getClass());
            assertEquals("Please provide key serializer class", e.getMessage());
        }
    }

    @Test
    public void testBuildNoValueSerializer() {
        try {
            Properties props = new ProducerPropertiesBuilder()
                    .withBoostrapServers("localhost:9092")
                    .withAcks(ProducerAcks.ALL)
                    .withKeySerializer(StringSerializer.class)
                    .build();
        } catch (Exception e) {
            assertEquals(RemotingKafkaConfigurationException.class, e.getClass());
            assertEquals("Please provide value serializer class", e.getMessage());
        }
    }
}
