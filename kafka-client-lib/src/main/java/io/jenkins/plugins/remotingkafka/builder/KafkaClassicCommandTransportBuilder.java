package io.jenkins.plugins.remotingkafka.builder;

import hudson.remoting.Capability;
import io.jenkins.plugins.remotingkafka.commandtransport.KafkaClassicCommandTransport;
import io.jenkins.plugins.remotingkafka.exception.RemotingKafkaTransportException;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;

import javax.annotation.CheckForNull;

/**
 * Builder class to build KafkaClassicCommandTransport
 */
public class KafkaClassicCommandTransportBuilder {
    @CheckForNull
    private Capability remoteCapability;

    @CheckForNull
    private Producer<String, byte[]> producer;

    @CheckForNull
    private Consumer<String, byte[]> consumer;

    @CheckForNull
    private String producerTopic;

    @CheckForNull
    private String producerKey;

    @CheckForNull
    private String consumerTopic;

    @CheckForNull
    private String consumerKey;

    private long pollTimeout;
    private int producerPartition;
    private int consumerPartition;

    public KafkaClassicCommandTransportBuilder withRemoteCapability(Capability cap) {
        this.remoteCapability = cap;
        return this;
    }

    public KafkaClassicCommandTransportBuilder withProducer(Producer<String, byte[]> producer) {
        this.producer = producer;
        return this;
    }

    public KafkaClassicCommandTransportBuilder withConsumer(Consumer<String, byte[]> consumer) {
        this.consumer = consumer;
        return this;
    }

    public KafkaClassicCommandTransportBuilder withProducerTopic(String producerTopic) {
        this.producerTopic = producerTopic;
        return this;
    }

    public KafkaClassicCommandTransportBuilder withProducerKey(String producerKey) {
        this.producerKey = producerKey;
        return this;
    }

    public KafkaClassicCommandTransportBuilder withConsumerTopic(String consumerTopic) {
        this.consumerTopic = consumerTopic;
        return this;
    }

    public KafkaClassicCommandTransportBuilder withConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
        return this;
    }

    public KafkaClassicCommandTransportBuilder withPollTimeout(long pollTimeout) {
        this.pollTimeout = pollTimeout;
        return this;
    }

    public KafkaClassicCommandTransportBuilder withProducerPartition(int producerPartition) {
        this.producerPartition = producerPartition;
        return this;
    }

    public KafkaClassicCommandTransportBuilder withConsumerPartition(int consumerPartition) {
        this.consumerPartition = consumerPartition;
        return this;
    }

    public KafkaClassicCommandTransport build() throws RemotingKafkaTransportException {
        if (remoteCapability == null) {
            throw new RemotingKafkaTransportException("Please provide remote capability");
        }
        if (producer == null) {
            throw new RemotingKafkaTransportException("Please provide a producer instance");
        }
        if (consumer == null) {
            throw new RemotingKafkaTransportException("Please provide a consumer instance");
        }
        if (producerTopic == null) {
            throw new RemotingKafkaTransportException("Please provide a producer topic");
        }
        if (producerKey == null) {
            throw new RemotingKafkaTransportException("Please provide a producer key");
        }
        if (consumerTopic == null) {
            throw new RemotingKafkaTransportException("Please provide a consumer topic");
        }
        if (consumerKey == null) {
            throw new RemotingKafkaTransportException("Please provide a consumer key");
        }
        return new KafkaClassicCommandTransport(this);
    }

    @CheckForNull
    public Capability getRemoteCapability() {
        return remoteCapability;
    }

    @CheckForNull
    public Producer<String, byte[]> getProducer() {
        return producer;
    }

    @CheckForNull
    public Consumer<String, byte[]> getConsumer() {
        return consumer;
    }

    @CheckForNull
    public String getProducerTopic() {
        return producerTopic;
    }

    @CheckForNull
    public String getProducerKey() {
        return producerKey;
    }

    @CheckForNull
    public String getConsumerTopic() {
        return consumerTopic;
    }

    @CheckForNull
    public String getConsumerKey() {
        return consumerKey;
    }

    public long getPollTimeout() {
        return pollTimeout;
    }

    public int getProducerPartition() {
        return producerPartition;
    }

    public int getConsumerPartition() {
        return consumerPartition;
    }
}
