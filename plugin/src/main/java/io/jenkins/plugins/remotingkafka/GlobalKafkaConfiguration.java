package io.jenkins.plugins.remotingkafka;

import hudson.Extension;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

@Extension
public class GlobalKafkaConfiguration extends GlobalConfiguration {
    private String connectionURL;
    private String zookeeperURL;

    public GlobalKafkaConfiguration() {
        load();
    }

    public static GlobalKafkaConfiguration get() {
        return GlobalConfiguration.all().get(GlobalKafkaConfiguration.class);
    }

    public String getConnectionURL() {
        return connectionURL;
    }

    public String getZookeeperURL() {
        return zookeeperURL;
    }

    public FormValidation doCheckConnectionURL(@QueryParameter String connectionURL) {
        if (StringUtils.isBlank(connectionURL)) {
            return FormValidation.warning("Please specify a Kafka connection URL");
        }
        return FormValidation.ok();
    }

    public FormValidation doCheckZookeeperURL(@QueryParameter String zookeeperURL) {
        if (StringUtils.isBlank(zookeeperURL)) {
            return FormValidation.warning("Please specify a Zookeeper URL");
        }
        return FormValidation.ok();
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        this.connectionURL = json.getString("connectionURL");
        this.zookeeperURL = json.getString("zookeeperURL");
        save();
        return true;
    }
}
