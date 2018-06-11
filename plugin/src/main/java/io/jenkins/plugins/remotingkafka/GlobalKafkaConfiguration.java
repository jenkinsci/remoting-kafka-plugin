package io.jenkins.plugins.remotingkafka;

import hudson.Extension;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.util.Properties;

@Extension
public class GlobalKafkaConfiguration extends GlobalConfiguration {
    private String connectionURL;
    private String consumerGroupID;
    private Properties producerProps;
    private Properties consumerProps;

    public static GlobalKafkaConfiguration get() {
        return GlobalConfiguration.all().get(GlobalKafkaConfiguration.class);
    }

    public GlobalKafkaConfiguration() {
        load();
    }

    public String getConnectionURL() {
        return connectionURL;
    }

    public String getConsumerGroupID() {
        return consumerGroupID;
    }

    public Properties getProducerProps() {
        return producerProps;
    }

    public Properties getConsumerProps() {
        return consumerProps;
    }

    public FormValidation doCheckConnectionURL(@QueryParameter String connectionURL) {
        if (StringUtils.isBlank(connectionURL)) {
            return FormValidation.warning("Please specify a Kafka connection URL");
        }
        return FormValidation.ok();
    }

    public FormValidation doCheckConsumerGroupID(@QueryParameter String consumerGroupID) {
        if (StringUtils.isBlank(consumerGroupID)) {
            return FormValidation.warning("Please specify a Kafka consumer group ID");
        }
        return FormValidation.ok();
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        this.connectionURL = json.getString("connectionURL");
        this.consumerGroupID = json.getString("consumerGroupID");
        setupProducerProps();
        setupConsumerProps();
        save();
        return true;
    }

    private final void setupProducerProps() {
        producerProps = new Properties();
        producerProps.put(KafkaConstants.BOOTSTRAP_SERVERS, connectionURL);
        producerProps.put(KafkaConstants.ACKS, "all");
    }

    private final void setupConsumerProps() {
        consumerProps = new Properties();
        consumerProps.put(KafkaConstants.BOOTSTRAP_SERVERS, connectionURL);
        consumerProps.put(KafkaConstants.GROUP_ID, consumerGroupID);
        consumerProps.put(KafkaConstants.ENABLE_AUTO_COMMIT, "false");
    }
}
