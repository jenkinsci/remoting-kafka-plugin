package io.jenkins.plugins.remotingkafka.builder;

import io.jenkins.plugins.remotingkafka.security.KafkaPasswordManager;

public class KafkaPasswordManagerBuilder {
    private String kafkaPassword;

    private String sslTruststorePassword;

    private String sslKeystorePassword;

    private String sslKeyPassword;

    public KafkaPasswordManagerBuilder withKafkaPassword(String password) {
        this.kafkaPassword = password;
        return this;
    }

    public KafkaPasswordManagerBuilder withSSLTruststorePassword(String password) {
        this.sslTruststorePassword = password;
        return this;
    }

    public KafkaPasswordManagerBuilder withSSLKeystorePassword(String password) {
        this.sslKeystorePassword = password;
        return this;
    }

    public KafkaPasswordManagerBuilder withSSLKeyPassword(String password) {
        this.sslKeyPassword = password;
        return this;
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

    public KafkaPasswordManager build() {
        return new KafkaPasswordManager(this);
    }
}
