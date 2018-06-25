package io.jenkins.plugins.remotingkafka.exception;

public class RemotingKafkaException extends Exception {
    RemotingKafkaException(String message) {
        super(message);
    }

    RemotingKafkaException(String message, Throwable cause) {
        super(message, cause);
    }
}
