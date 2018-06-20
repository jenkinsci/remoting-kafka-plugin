package io.jenkins.plugins.remoting.kafka.builder;


import io.jenkins.plugins.remotingkafka.KafkaConfigs;
import io.jenkins.plugins.remotingkafka.builder.ProducerAcks;
import io.jenkins.plugins.remotingkafka.builder.ProducerPropertiesBuilder;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Properties;

public class ProducerPropertiesBuilderTest {
    @Test
    public void testBuildAllConfigs() {
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
    public void testBuildNoAcks() {
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
            assertEquals("Please provide value serializer class", e.getMessage());
        }
    }
}
