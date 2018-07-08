package io.jenkins.plugins.remoting.kafka.builder;

import io.jenkins.plugins.remotingkafka.KafkaConfigs;
import io.jenkins.plugins.remotingkafka.builder.SecurityPropertiesBuilder;
import io.jenkins.plugins.remotingkafka.enums.SecurityProtocol;
import io.jenkins.plugins.remotingkafka.exception.RemotingKafkaSecurityException;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class SecurityPropertiesBuilderTest {
    @Test
    public void testPositiveCase() throws RemotingKafkaSecurityException {
        Properties props = new SecurityPropertiesBuilder()
                .withSecurityProtocol(SecurityProtocol.SSL)
                .withSSLKeystoreLocation("test")
                .withSSLKeystorePassword("test")
                .withSSLTruststoreLocation("test")
                .withSSLTruststorePassword("test")
                .withSSLKeyPassword("test")
                .withSASLJassConfig("test", "test")
                .withSASLMechanism("test")
                .build();
        assertEquals(SecurityProtocol.SSL.toString(), props.get(KafkaConfigs.SECURITY_PROTOCOL));
        assertEquals("test", props.get(KafkaConfigs.SSL_KEYSTORE_LOCATION));
        assertEquals("test", props.get(KafkaConfigs.SSL_KEYSTORE_PASSWORD));
        assertEquals("test", props.get(KafkaConfigs.SSL_KEY_PASSWORD));
        assertEquals("test", props.get(KafkaConfigs.SSL_TRUSTSTORE_LOCATION));
        assertEquals("test", props.get(KafkaConfigs.SSL_TRUSTSTORE_PASSWORD));
        assertEquals("org.apache.kafka.common.security.plain.PlainLoginModule required username=\""
                + "test" + "\" password=" + "\"" + "test" + "\";", props.get(KafkaConfigs.SASL_JAAS_CONFIG));
        assertEquals("test", props.get(KafkaConfigs.SASL_MECHANISM));
    }
}
