package io.jenkins.plugins.remotingkafka.exception;

public class RemotingKafkaSecurityException extends RemotingKafkaException {
    public RemotingKafkaSecurityException(String message) {
        super(message);
    }

    public RemotingKafkaSecurityException(String message, Throwable cause) {
        super(message, cause);
    }
}
