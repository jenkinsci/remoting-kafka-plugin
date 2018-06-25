package io.jenkins.plugins.remotingkafka.exception;

public class RemotingKafkaConfigurationException extends RemotingKafkaException {
    public RemotingKafkaConfigurationException(String message) {
        super(message);
    }

    public RemotingKafkaConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
