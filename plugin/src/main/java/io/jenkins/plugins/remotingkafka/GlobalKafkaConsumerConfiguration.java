package io.jenkins.plugins.remotingkafka;

import hudson.Extension;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.util.Properties;

/**
 * Global configuration for Kafka(1.1.0) consumer, according to Kafka documentation
 * https://kafka.apache.org/documentation/#consumerconfigs.
 */
@Extension
public class GlobalKafkaConsumerConfiguration extends GlobalConfiguration {
    private String bootstrapServers;
    private String groupID;
    private Properties props;

    public GlobalKafkaConsumerConfiguration() {
        load();
    }

    @DataBoundConstructor
    public GlobalKafkaConsumerConfiguration(String bootstrapServers, String groupID) {
        props = new Properties();
        this.bootstrapServers = bootstrapServers;
        this.groupID = groupID;
        props.put(KafkaConstants.BOOTSTRAP_SERVERS, bootstrapServers);
        props.put(KafkaConstants.GROUP_ID, groupID);
        props.put(KafkaConstants.ENABLE_AUTO_COMMIT, "false");
        save();
    }

    public static GlobalKafkaConsumerConfiguration get() {
        return GlobalConfiguration.all().get(GlobalKafkaConsumerConfiguration.class);
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

    public String getGroupID() {
        return groupID;
    }

    public Properties getProps() {
        return this.props;
    }
}
