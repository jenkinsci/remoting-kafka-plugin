package io.jenkins.plugins.remoting.kafka.builder;

import io.jenkins.plugins.remotingkafka.KafkaConfigs;
import io.jenkins.plugins.remotingkafka.builder.AutoOffsetReset;
import io.jenkins.plugins.remotingkafka.builder.ConsumerPropertiesBuilder;
import io.jenkins.plugins.remotingkafka.exception.RemotingKafkaConfigurationException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class ConsumerPropertiesBuilderTest {
    @Test
    public void testBuildAllConfigs() throws RemotingKafkaConfigurationException {
        Properties props = new ConsumerPropertiesBuilder()
                .withBootstrapServers("localhost:9092")
                .withEnableAutoCommit(false)
                .withAutoOffsetReset(AutoOffsetReset.EARLIEST)
                .withGroupID("test")
                .withKeyDeserializer(StringDeserializer.class)
                .withValueDeserializer(StringDeserializer.class)
                .build();
        assertEquals("localhost:9092", props.get(KafkaConfigs.BOOTSTRAP_SERVERS));
        assertEquals(false, props.get(KafkaConfigs.ENABLE_AUTO_COMMIT));
        assertEquals(AutoOffsetReset.EARLIEST.toString(), props.get(KafkaConfigs.AUTO_OFFSET_RESET));
        assertEquals("test", props.get(KafkaConfigs.GROUP_ID));
        assertEquals(StringDeserializer.class, props.get(KafkaConfigs.KEY_DESERIALIZER));
        assertEquals(StringDeserializer.class, props.get(KafkaConfigs.VALUE_DESERIALIZER));
    }

    @Test
    public void testBuildNoEnableAutoCommit() throws RemotingKafkaConfigurationException {
        Properties props = new ConsumerPropertiesBuilder()
                .withBootstrapServers("localhost:9092")
                .withAutoOffsetReset(AutoOffsetReset.EARLIEST)
                .withGroupID("test")
                .withKeyDeserializer(StringDeserializer.class)
                .withValueDeserializer(StringDeserializer.class)
                .build();
        assertEquals("localhost:9092", props.get(KafkaConfigs.BOOTSTRAP_SERVERS));
        assertEquals(true, props.get(KafkaConfigs.ENABLE_AUTO_COMMIT));
        assertEquals(AutoOffsetReset.EARLIEST.toString(), props.get(KafkaConfigs.AUTO_OFFSET_RESET));
        assertEquals("test", props.get(KafkaConfigs.GROUP_ID));
        assertEquals(StringDeserializer.class, props.get(KafkaConfigs.KEY_DESERIALIZER));
        assertEquals(StringDeserializer.class, props.get(KafkaConfigs.VALUE_DESERIALIZER));
    }

    @Test
    public void testBuildNoAutoOffsetReset() throws RemotingKafkaConfigurationException {
        Properties props = new ConsumerPropertiesBuilder()
                .withBootstrapServers("localhost:9092")
                .withEnableAutoCommit(false)
                .withGroupID("test")
                .withKeyDeserializer(StringDeserializer.class)
                .withValueDeserializer(StringDeserializer.class)
                .build();
        assertEquals("localhost:9092", props.get(KafkaConfigs.BOOTSTRAP_SERVERS));
        assertEquals(false, props.get(KafkaConfigs.ENABLE_AUTO_COMMIT));
        assertEquals(null, props.get(KafkaConfigs.AUTO_OFFSET_RESET));
        assertEquals("test", props.get(KafkaConfigs.GROUP_ID));
        assertEquals(StringDeserializer.class, props.get(KafkaConfigs.KEY_DESERIALIZER));
        assertEquals(StringDeserializer.class, props.get(KafkaConfigs.VALUE_DESERIALIZER));
    }

    @Test
    public void testBuildNoBootstrapServers() {
        try {
            Properties props = new ConsumerPropertiesBuilder()
                    .withEnableAutoCommit(false)
                    .withAutoOffsetReset(AutoOffsetReset.EARLIEST)
                    .withGroupID("test")
                    .withKeyDeserializer(StringDeserializer.class)
                    .withValueDeserializer(StringDeserializer.class)
                    .build();
        } catch (Exception e) {
            assertEquals(RemotingKafkaConfigurationException.class, e.getClass());
            assertEquals("Please provide Kafka consumer bootstrap servers", e.getMessage());
        }
    }

    @Test
    public void testBuildNoKeyDeserializer() {
        try {
            Properties props = new ConsumerPropertiesBuilder()
                    .withBootstrapServers("localhost:9092")
                    .withEnableAutoCommit(false)
                    .withAutoOffsetReset(AutoOffsetReset.EARLIEST)
                    .withGroupID("test")
                    .withValueDeserializer(StringDeserializer.class)
                    .build();
        } catch (Exception e) {
            assertEquals(RemotingKafkaConfigurationException.class, e.getClass());
            assertEquals("Please provide key deserializer class", e.getMessage());
        }
    }

    @Test
    public void testBuildNoValueDeserializer() {
        try {
            Properties props = new ConsumerPropertiesBuilder()
                    .withBootstrapServers("localhost:9092")
                    .withEnableAutoCommit(false)
                    .withAutoOffsetReset(AutoOffsetReset.EARLIEST)
                    .withGroupID("test")
                    .withKeyDeserializer(StringDeserializer.class)
                    .build();
        } catch (Exception e) {
            assertEquals(RemotingKafkaConfigurationException.class, e.getClass());
            assertEquals("Please provide value deserializer class", e.getMessage());
        }
    }
}
