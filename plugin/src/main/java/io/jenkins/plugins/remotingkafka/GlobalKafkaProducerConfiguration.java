package io.jenkins.plugins.remotingkafka;

import hudson.Extension;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.util.Properties;

/**
 * Global configuration for Kafka(1.1.0) producer, according to Kafka documentation
 * https://kafka.apache.org/documentation/#producerconfigs.
 */
@Extension
public class GlobalKafkaProducerConfiguration extends GlobalConfiguration {
    private String bootstrapServers;
    private Properties props;

    public GlobalKafkaProducerConfiguration() {
        load();
    }

    @DataBoundConstructor
    public GlobalKafkaProducerConfiguration(String bootstrapServers) {
        props = new Properties();
        this.bootstrapServers = bootstrapServers;
        props.put(KafkaConstants.BOOTSTRAP_SERVERS, bootstrapServers);
        props.put(KafkaConstants.ACKS, "all");
        save();
    }

    public static GlobalKafkaProducerConfiguration get() {
        return GlobalConfiguration.all().get(GlobalKafkaProducerConfiguration.class);
    }

    public FormValidation doCheckConnectionURL(@QueryParameter String bootstrapServers) {
        if (StringUtils.isBlank(bootstrapServers)) {
            return FormValidation.warning("Please specify a bootstrap server");
        }
        return FormValidation.ok();
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public Properties getProps() {
        return this.props;
    }
}
