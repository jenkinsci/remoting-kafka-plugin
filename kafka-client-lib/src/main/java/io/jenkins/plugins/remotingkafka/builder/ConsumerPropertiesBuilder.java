package io.jenkins.plugins.remotingkafka.builder;

import io.jenkins.plugins.remotingkafka.KafkaConfigs;
import io.jenkins.plugins.remotingkafka.enums.AutoOffsetReset;
import io.jenkins.plugins.remotingkafka.exception.RemotingKafkaConfigurationException;

import javax.annotation.CheckForNull;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Builder class to build Kafka consumer properties, with references from https://kafka.apache.org/documentation/#consumerconfigs.
 */
public class ConsumerPropertiesBuilder {
    private static final Logger LOGGER = Logger.getLogger(ConsumerPropertiesBuilder.class.getName());

    /**
     * A list of host/port pairs to use for establishing the initial connection to the Kafka cluster.
     * We use a single cluster for now.
     */
    @CheckForNull
    private String bootstrapServers;

    /**
     * If true the consumer's offset will be periodically committed in the background.
     */
    private boolean enableAutoCommit;

    /**
     * A unique string that identifies the consumer group this consumer belongs to.
     */
    private String groupID;

    /**
     * What to do when there is no initial offset in Kafka or if the current offset does not exist any more on the server.
     */
    private AutoOffsetReset autoOffsetReset;

    /**
     * Deserializer class for key that implements the org.apache.kafka.common.serialization.Deserializer interface.
     */
    @CheckForNull
    private Class keyDeserializer;

    /**
     * Deserializer class for value that implements the org.apache.kafka.common.serialization.Deserializer interface.
     */
    @CheckForNull
    private Class valueDeserializer;

    @CheckForNull
    private Properties securityProps;

    public ConsumerPropertiesBuilder() {
        this.enableAutoCommit = true;
    }

    public ConsumerPropertiesBuilder withBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
        return this;
    }

    public ConsumerPropertiesBuilder withEnableAutoCommit(boolean enableAutoCommit) {
        this.enableAutoCommit = enableAutoCommit;
        return this;
    }

    public ConsumerPropertiesBuilder withGroupID(String groupID) {
        this.groupID = groupID;
        return this;
    }

    public ConsumerPropertiesBuilder withAutoOffsetReset(AutoOffsetReset autoOffsetReset) {
        this.autoOffsetReset = autoOffsetReset;
        return this;
    }

    public ConsumerPropertiesBuilder withKeyDeserializer(Class keyDeserializer) {
        this.keyDeserializer = keyDeserializer;
        return this;
    }

    public ConsumerPropertiesBuilder withValueDeserializer(Class valueDeserializer) {
        this.valueDeserializer = valueDeserializer;
        return this;
    }

    public ConsumerPropertiesBuilder withSecurityProps(Properties props) {
        this.securityProps = props;
        return this;
    }


    public Properties build() throws RemotingKafkaConfigurationException {
        Properties props = (securityProps == null) ? new Properties() : securityProps;
        if (bootstrapServers == null) {
            throw new RemotingKafkaConfigurationException("Please provide Kafka consumer bootstrap servers");
        }
        props.put(KafkaConfigs.BOOTSTRAP_SERVERS, bootstrapServers);
        props.put(KafkaConfigs.ENABLE_AUTO_COMMIT, enableAutoCommit);
        if (groupID != null) {
            props.put(KafkaConfigs.GROUP_ID, groupID);
        }
        if (autoOffsetReset != null) {
            props.put(KafkaConfigs.AUTO_OFFSET_RESET, autoOffsetReset.toString());
        }
        if (keyDeserializer == null) {
            throw new RemotingKafkaConfigurationException("Please provide key deserializer class");
        }
        props.put(KafkaConfigs.KEY_DESERIALIZER, keyDeserializer);
        if (valueDeserializer == null) {
            throw new RemotingKafkaConfigurationException("Please provide value deserializer class");
        }
        props.put(KafkaConfigs.VALUE_DESERIALIZER, valueDeserializer);

        return props;
    }
}
