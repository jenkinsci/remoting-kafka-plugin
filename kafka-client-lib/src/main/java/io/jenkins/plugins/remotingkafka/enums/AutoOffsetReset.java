package io.jenkins.plugins.remotingkafka.enums;

/**
 * Enum for auto.offset.reset in Kafka Consumer.
 */
public enum AutoOffsetReset {
    /**
     * Automatically reset the offset to the earliest offset.
     */
    EARLIEST {
        @Override
        public String toString() {
            return "earliest";
        }
    },
    /**
     * Automatically reset the offset to the earliest offset.
     */
    LATEST {
        @Override
        public String toString() {
            return "latest";
        }
    },
    /**
     * Throw exception to the consumer if no previous offset is found for the consumer's group.
     */
    NONE {
        @Override
        public String toString() {
            return "none";
        }
    }
}
