package io.jenkins.plugins.remotingkafka;

import hudson.Extension;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

@Extension
public class GlobalKafkaConfiguration extends GlobalConfiguration {
    private String connectionURL;

    public GlobalKafkaConfiguration() {
        load();
    }

    public static GlobalKafkaConfiguration get() {
        return GlobalConfiguration.all().get(GlobalKafkaConfiguration.class);
    }

    public FormValidation doCheckConnectionURL(@QueryParameter String connectionURL) {
        if (StringUtils.isBlank(connectionURL)) {
            return FormValidation.warning("Please specify a connection URL");
        }
        return FormValidation.ok();
    }

    public String getConnectionURL() {
        return connectionURL;
    }

    @DataBoundSetter
    public void setConnectionURL(String connectionURL) {
        this.connectionURL = connectionURL;
        save();
    }
}
