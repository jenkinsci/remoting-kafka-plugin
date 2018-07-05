package io.jenkins.plugins.remoting.kafka.builder;

import io.jenkins.plugins.remotingkafka.KafkaConfigs;
import io.jenkins.plugins.remotingkafka.builder.SecurityPropertiesBuilder;
import io.jenkins.plugins.remotingkafka.enums.SecurityProtocol;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class SecurityPropertiesBuilderTest {
    @Test
    public void testWithSecurityProtocol() {
        Properties props = new SecurityPropertiesBuilder().withSecurityProtocol(SecurityProtocol.SSL).build();
        assertEquals(SecurityProtocol.SSL.toString(), props.get(KafkaConfigs.SECURITY_PROTOCOL));
    }

    @Test
    public void testWithSSLKeystoreLocation() {
        Properties props = new SecurityPropertiesBuilder().withSSLKeystoreLocation("test").build();
        assertEquals("test", props.get(KafkaConfigs.SSL_KEYSTORE_LOCATION));
    }

    @Test
    public void testWithSSLKeystorePassword() {
        Properties props = new SecurityPropertiesBuilder().withSSLKeystorePassword("test").build();
        assertEquals("test", props.get(KafkaConfigs.SSL_KEYSTORE_PASSWORD));
    }

    @Test
    public void testWithSSLKeyPassword() {
        Properties props = new SecurityPropertiesBuilder().withSSLKeyPassword("test").build();
        assertEquals("test", props.get(KafkaConfigs.SSL_KEY_PASSWORD));
    }

    @Test
    public void testWithSSLTruststoreLocation() {
        Properties props = new SecurityPropertiesBuilder().withSSLTruststoreLocation("test").build();
        assertEquals("test", props.get(KafkaConfigs.SSL_TRUSTSTORE_LOCATION));
    }

    @Test
    public void testWithSSLTruststorePassword() {
        Properties props = new SecurityPropertiesBuilder().withSSLTruststorePassword("test").build();
        assertEquals("test", props.get(KafkaConfigs.SSL_TRUSTSTORE_PASSWORD));
    }

    @Test
    public void testWithSaslJassConfig() {
        Properties props = new SecurityPropertiesBuilder().withSASLJassConfig("test", "test").build();
        assertEquals("org.apache.kafka.common.security.plain.PlainLoginModule required username=\""
                + "test" + "\" password=" + "\"" + "test" + "\";", props.get(KafkaConfigs.SASL_JAAS_CONFIG));
    }

    @Test
    public void testWithSASLMechanism() {
        Properties props = new SecurityPropertiesBuilder().withSASLMechanism("test").build();
        assertEquals("test", props.get(KafkaConfigs.SASL_MECHANISM));
    }
}
