package io.jenkins.plugins.remotingkafka.builder;

import io.jenkins.plugins.remotingkafka.KafkaConfigs;
import io.jenkins.plugins.remotingkafka.enums.SecurityProtocol;
import io.jenkins.plugins.remotingkafka.exception.RemotingKafkaSecurityException;

import javax.annotation.CheckForNull;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Builder class to build security properties to be used in Kafka consumer and producer client.
 */
public class SecurityPropertiesBuilder {
    private static final Logger LOGGER = Logger.getLogger(SecurityPropertiesBuilder.class.getName());

    /**
     * The location of the trust store file.
     */
    @CheckForNull
    private String sslTruststoreLocation;

    /**
     * The password for the trust store file.
     */
    @CheckForNull
    private String sslTruststorePassword;

    /**
     * The location of the key store file.
     */
    @CheckForNull
    private String sslKeystoreLocation;

    /**
     * The store password for the key store file.
     */
    @CheckForNull
    private String sslKeystorePassword;

    /**
     * The password of the private key in the key store file.
     */
    @CheckForNull
    private String sslKeyPassword;

    /**
     * JAAS login context parameters for SASL connections in the format used by JAAS configuration files.
     */
    @CheckForNull
    private String saslJassConfig;

    /**
     * Protocol used to communicate with brokers.
     */
    @CheckForNull
    private SecurityProtocol securityProtocol;

    /**
     * SASL mechanism used for client connections.
     */
    @CheckForNull
    private String saslMechanism;

    public SecurityPropertiesBuilder withSSLTruststoreLocation(String location) {
        this.sslTruststoreLocation = location;
        return this;
    }

    public SecurityPropertiesBuilder withSSLTruststorePassword(String password) {
        this.sslTruststorePassword = password;
        return this;
    }

    public SecurityPropertiesBuilder withSSLKeystoreLocation(String location) {
        this.sslKeystoreLocation = location;
        return this;
    }

    public SecurityPropertiesBuilder withSSLKeystorePassword(String password) {
        this.sslKeystorePassword = password;
        return this;
    }

    public SecurityPropertiesBuilder withSSLKeyPassword(String password) {
        this.sslKeyPassword = password;
        return this;
    }

    public SecurityPropertiesBuilder withSASLJassConfig(String username, String password) {
        this.saslJassConfig = "org.apache.kafka.common.security.plain.PlainLoginModule required username=\""
                + username + "\" password=" + "\"" + password + "\";";
        return this;
    }

    public SecurityPropertiesBuilder withSecurityProtocol(SecurityProtocol protocol) {
        this.securityProtocol = protocol;
        return this;
    }

    public SecurityPropertiesBuilder withSASLMechanism(String mechanism) {
        this.saslMechanism = mechanism;
        return this;
    }

    private void put(Properties props, String key, Object value) throws RemotingKafkaSecurityException {
        if (value != null) {
            props.put(key, value);
        } else {
            throw new RemotingKafkaSecurityException("Please provide " + key);
        }
    }

    public Properties build() throws RemotingKafkaSecurityException {
        Properties props = new Properties();
        put(props, KafkaConfigs.SSL_KEYSTORE_LOCATION, sslKeystoreLocation);
        put(props, KafkaConfigs.SSL_KEYSTORE_PASSWORD, sslKeystorePassword);
        put(props, KafkaConfigs.SSL_TRUSTSTORE_LOCATION, sslTruststoreLocation);
        put(props, KafkaConfigs.SSL_TRUSTSTORE_PASSWORD, sslTruststorePassword);
        put(props, KafkaConfigs.SSL_KEY_PASSWORD, sslKeyPassword);
        put(props, KafkaConfigs.SASL_JAAS_CONFIG, saslJassConfig);
        if (securityProtocol != null) {
            put(props, KafkaConfigs.SECURITY_PROTOCOL, securityProtocol.toString());
        }
        put(props, KafkaConfigs.SASL_MECHANISM, saslMechanism);
        return props;
    }
}
