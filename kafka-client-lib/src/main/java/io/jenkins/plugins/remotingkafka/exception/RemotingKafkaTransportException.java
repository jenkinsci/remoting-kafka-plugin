package io.jenkins.plugins.remotingkafka.exception;

public class RemotingKafkaTransportException extends RemotingKafkaException {
    public RemotingKafkaTransportException(String message) {
        super(message);
    }

    public RemotingKafkaTransportException(String message, Throwable cause) {
        super(message, cause);
    }
}
