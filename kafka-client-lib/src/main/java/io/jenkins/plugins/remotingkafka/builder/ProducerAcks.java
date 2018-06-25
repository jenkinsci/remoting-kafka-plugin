package io.jenkins.plugins.remotingkafka.builder;

/**
 * Enum for acks in KafkaProducer.
 */
public enum ProducerAcks {
    /**
     * This means the leader will wait for the full set of in-sync replicas to acknowledge the record.
     */
    ALL {
        @Override
        public String toString() {
            return "all";
        }
    },
    /**
     * If set to zero then the producer will not wait for any acknowledgment from the server at all.
     */
    ZERO {
        @Override
        public String toString() {
            return "0";
        }
    },
    /**
     * This will mean the leader will write the record to its local log but will respond
     * without awaiting full acknowledgement from all followers.
     */
    ONE {
        @Override
        public String toString() {
            return "1";
        }
    },
}
