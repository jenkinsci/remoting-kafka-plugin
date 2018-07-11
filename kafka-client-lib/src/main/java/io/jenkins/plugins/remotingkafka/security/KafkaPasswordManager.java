package io.jenkins.plugins.remotingkafka.security;

import io.jenkins.plugins.remotingkafka.builder.KafkaPasswordManagerBuilder;

public class KafkaPasswordManager {
    private final String kafkaPassword;

    private final String sslTruststorePassword;

    private final String sslKeystorePassword;

    private final String sslKeyPassword;

    public KafkaPasswordManager(KafkaPasswordManagerBuilder settings) {
        this.kafkaPassword = settings.getKafkaPassword();
        this.sslTruststorePassword = settings.getSslTruststorePassword();
        this.sslKeystorePassword = settings.getSslKeystorePassword();
        this.sslKeyPassword = settings.getSslKeyPassword();
    }

    public String getKafkaPassword() {
        return kafkaPassword;
    }

    public String getSslTruststorePassword() {
        return sslTruststorePassword;
    }

    public String getSslKeystorePassword() {
        return sslKeystorePassword;
    }

    public String getSslKeyPassword() {
        return sslKeyPassword;
    }
}
