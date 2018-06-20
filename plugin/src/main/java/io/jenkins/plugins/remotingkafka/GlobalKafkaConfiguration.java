package io.jenkins.plugins.remotingkafka;

import hudson.Extension;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.util.Properties;

@Extension
public class GlobalKafkaConfiguration extends GlobalConfiguration {
    private String connectionURL;

    public static GlobalKafkaConfiguration get() {
        return GlobalConfiguration.all().get(GlobalKafkaConfiguration.class);
    }

    public GlobalKafkaConfiguration() {
        load();
    }

    public String getConnectionURL() {
        return connectionURL;
    }

    public FormValidation doCheckConnectionURL(@QueryParameter String connectionURL) {
        if (StringUtils.isBlank(connectionURL)) {
            return FormValidation.warning("Please specify a Kafka connection URL");
        }
        return FormValidation.ok();
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        this.connectionURL = json.getString("connectionURL");
        save();
        return true;
    }
}
