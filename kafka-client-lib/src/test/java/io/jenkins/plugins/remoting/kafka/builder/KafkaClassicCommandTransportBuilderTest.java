package io.jenkins.plugins.remoting.kafka.builder;

import hudson.remoting.Capability;
import io.jenkins.plugins.remotingkafka.builder.KafkaClassicCommandTransportBuilder;
import static org.junit.Assert.*;
import org.junit.Test;

public class KafkaClassicCommandTransportBuilderTest {
    @Test
    public void testWithCapability() {
        KafkaClassicCommandTransportBuilder builder = new KafkaClassicCommandTransportBuilder()
                .withRemoteCapability(new Capability());
        assertNotEquals(null, builder.getRemoteCapability());
    }

    @Test
    public void testWithProducerTopic() {
        KafkaClassicCommandTransportBuilder builder = new KafkaClassicCommandTransportBuilder()
                .withProducerTopic("test");
        assertEquals("test", builder.getProducerTopic());
    }

    @Test
    public void testWithConsumerTopic() {
        KafkaClassicCommandTransportBuilder builder = new KafkaClassicCommandTransportBuilder()
                .withConsumerTopic("test");
        assertEquals("test", builder.getConsumerTopic());
    }


    @Test
    public void testWithProducerKey() {
        KafkaClassicCommandTransportBuilder builder = new KafkaClassicCommandTransportBuilder()
                .withProducerKey("test");
        assertEquals("test", builder.getProducerKey());
    }


    @Test
    public void testWithConsumerKey() {
        KafkaClassicCommandTransportBuilder builder = new KafkaClassicCommandTransportBuilder()
                .withConsumerKey("test");
        assertEquals("test", builder.getConsumerKey());
    }


    @Test
    public void testWithConsumerPartition() {
        KafkaClassicCommandTransportBuilder builder = new KafkaClassicCommandTransportBuilder()
                .withConsumerPartition(0);
        assertEquals(0, builder.getConsumerPartition());
    }

    @Test
    public void testWithProducerPartition() {
        KafkaClassicCommandTransportBuilder builder = new KafkaClassicCommandTransportBuilder()
                .withProducerPartition(0);
        assertEquals(0, builder.getProducerPartition());
    }

    @Test
    public void testWithPollTimeout() {
        KafkaClassicCommandTransportBuilder builder = new KafkaClassicCommandTransportBuilder()
                .withPollTimeout(0);
        assertEquals(0, builder.getPollTimeout());
    }
}
