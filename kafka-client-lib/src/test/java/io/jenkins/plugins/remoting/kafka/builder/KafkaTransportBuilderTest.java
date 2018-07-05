package io.jenkins.plugins.remoting.kafka.builder;

import hudson.remoting.Capability;
import io.jenkins.plugins.remotingkafka.builder.KafkaTransportBuilder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class KafkaTransportBuilderTest {
    @Test
    public void testWithCapability() {
        KafkaTransportBuilder builder = new KafkaTransportBuilder()
                .withRemoteCapability(new Capability());
        assertNotEquals(null, builder.getRemoteCapability());
    }

    @Test
    public void testWithProducerTopic() {
        KafkaTransportBuilder builder = new KafkaTransportBuilder()
                .withProducerTopic("test");
        assertEquals("test", builder.getProducerTopic());
    }

    @Test
    public void testWithConsumerTopic() {
        KafkaTransportBuilder builder = new KafkaTransportBuilder()
                .withConsumerTopic("test");
        assertEquals("test", builder.getConsumerTopic());
    }


    @Test
    public void testWithProducerKey() {
        KafkaTransportBuilder builder = new KafkaTransportBuilder()
                .withProducerKey("test");
        assertEquals("test", builder.getProducerKey());
    }


    @Test
    public void testWithConsumerKey() {
        KafkaTransportBuilder builder = new KafkaTransportBuilder()
                .withConsumerKey("test");
        assertEquals("test", builder.getConsumerKey());
    }


    @Test
    public void testWithConsumerPartition() {
        KafkaTransportBuilder builder = new KafkaTransportBuilder()
                .withConsumerPartition(0);
        assertEquals(0, builder.getConsumerPartition());
    }

    @Test
    public void testWithProducerPartition() {
        KafkaTransportBuilder builder = new KafkaTransportBuilder()
                .withProducerPartition(0);
        assertEquals(0, builder.getProducerPartition());
    }

    @Test
    public void testWithPollTimeout() {
        KafkaTransportBuilder builder = new KafkaTransportBuilder()
                .withPollTimeout(0);
        assertEquals(0, builder.getPollTimeout());
    }
}
