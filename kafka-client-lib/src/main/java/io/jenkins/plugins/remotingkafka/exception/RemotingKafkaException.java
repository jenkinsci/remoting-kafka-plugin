package io.jenkins.plugins.remotingkafka.exception;

public class RemotingKafkaException extends Exception {
    RemotingKafkaException() {

    }

    RemotingKafkaException(String message) {
        super(message);
    }
}
