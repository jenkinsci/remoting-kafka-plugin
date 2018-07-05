package io.jenkins.plugins.remoting.kafka.builder;

import io.jenkins.plugins.remotingkafka.builder.KafkaPasswordManagerBuilder;
import io.jenkins.plugins.remotingkafka.security.KafkaPasswordManager;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class KafkaPasswordManagerTest {
    @Test
    public void testWithKafkaPassword() {
        KafkaPasswordManager passwordManager = new KafkaPasswordManagerBuilder().withKafkaPassword("test").build();
        assertEquals("test", passwordManager.getKafkaPassword());
    }

    @Test
    public void testWithSslTruststorePassword() {
        KafkaPasswordManager passwordManager = new KafkaPasswordManagerBuilder().withSSLTruststorePassword("test").build();
        assertEquals("test", passwordManager.getSslTruststorePassword());
    }

    @Test
    public void testWithSslKeystorePassword() {
        KafkaPasswordManager passwordManager = new KafkaPasswordManagerBuilder().withSSLKeystorePassword("test").build();
        assertEquals("test", passwordManager.getSslKeystorePassword());
    }

    @Test
    public void testWithSslKeyPassword() {
        KafkaPasswordManager passwordManager = new KafkaPasswordManagerBuilder().withSSLKeyPassword("test").build();
        assertEquals("test", passwordManager.getSslKeyPassword());
    }
}
