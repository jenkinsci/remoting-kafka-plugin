package io.jenkins.plugins.remotingkafka.builder;

import io.jenkins.plugins.remotingkafka.KafkaConfigs;

import javax.annotation.CheckForNull;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Builder class to build Kafka producer properties, with references from https://kafka.apache.org/documentation/#producerconfigs.
 */
public class ProducerPropertiesBuilder {
    private static final Logger LOGGER = Logger.getLogger(ProducerPropertiesBuilder.class.getName());

    /**
     * A list of host/port pairs to use for establishing the initial connection to the Kafka cluster.
     * We use a single cluster for now.
     */
    @CheckForNull
    private String bootsrapServers;

    /**
     * The number of acknowledgments the producer requires the leader to have received before considering a request complete.
     */
    private ProducerAcks acks;

    /**
     * Serializer class for key that implements the org.apache.kafka.common.serialization.Serializer interface.
     */
    @CheckForNull
    private Class keySerializer;

    /**
     * Serializer class for value that implements the org.apache.kafka.common.serialization.Serializer interface.
     */
    @CheckForNull
    private Class valueSerializer;

    public ProducerPropertiesBuilder withBoostrapServers(String boostrapServers) {
        this.bootsrapServers = boostrapServers;
        return this;
    }

    public ProducerPropertiesBuilder withAcks(ProducerAcks acks) {
        this.acks = acks;
        return this;
    }

    public ProducerPropertiesBuilder withKeySerializer(Class keySerializer) {
        this.keySerializer = keySerializer;
        return this;
    }

    public ProducerPropertiesBuilder withValueSerialier(Class valueSerialier) {
        this.valueSerializer = valueSerialier;
        return this;
    }

    public Properties build() {
        Properties props = new Properties();
        if (bootsrapServers == null) {
            throw new IllegalStateException("Please provide Kafka producer bootstrap servers");
        }
        props.put(KafkaConfigs.BOOTSTRAP_SERVERS, bootsrapServers);
        if (acks != null) {
            props.put(KafkaConfigs.ACKS, acks.toString());
        }
        if (keySerializer == null) {
            throw new IllegalStateException("Please provide key serializer class");
        }
        props.put(KafkaConfigs.KEY_SERIALIZER, keySerializer);
        if (valueSerializer == null) {
            throw new IllegalStateException("Please provide value serializer class");
        }
        props.put(KafkaConfigs.VALUE_SERIALIZER, valueSerializer);
        return props;
    }
}
