package io.jenkins.plugins.remotingkafka.exception;

public class RemotingKafkaTransportException extends RemotingKafkaException {
    public RemotingKafkaTransportException() {

    }

    public RemotingKafkaTransportException(String message) {
        super(message);
    }
}
