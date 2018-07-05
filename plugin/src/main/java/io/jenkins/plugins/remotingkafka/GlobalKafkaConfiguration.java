package io.jenkins.plugins.remotingkafka;

import hudson.Extension;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.IOException;
import java.net.Socket;

@Extension
public class GlobalKafkaConfiguration extends GlobalConfiguration {
    private String brokerURL;
    private String zookeeperURL;
    private String username;
    private String password;
    private String sslTruststoreLocation;
    private String sslTruststorePassword;
    private String sslKeystoreLocation;
    private String sslKeystorePassword;
    private String sslKeyPassword;

    public GlobalKafkaConfiguration() {
        load();
    }

    public static GlobalKafkaConfiguration get() {
        return GlobalConfiguration.all().get(GlobalKafkaConfiguration.class);
    }

    public String getBrokerURL() {
        return brokerURL;
    }

    public String getZookeeperURL() {
        return zookeeperURL;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getSslTruststoreLocation() {
        return sslTruststoreLocation;
    }

    public String getSslTruststorePassword() {
        return sslTruststorePassword;
    }

    public String getSslKeystoreLocation() {
        return sslKeystoreLocation;
    }

    public String getSslKeystorePassword() {
        return sslKeystorePassword;
    }

    public String getSslKeyPassword() {
        return sslKeyPassword;
    }

    public FormValidation doCheckBrokerURL(@QueryParameter("brokerURL") final String brokerURL) {
        if (StringUtils.isBlank(brokerURL)) {
            return FormValidation.error(Messages.GlobalKafkaConfiguration_KafkaConnectionURLWarning());
        }
        return FormValidation.ok();
    }

    public FormValidation doCheckZookeeperURL(@QueryParameter("zookeeperURL") String zookeeperURL) {
        if (StringUtils.isBlank(zookeeperURL)) {
            return FormValidation.error(Messages.GlobalKafkaConfiguration_ZookeeperURLWarning());
        }
        return FormValidation.ok();
    }

    public FormValidation doCheckUsername(@QueryParameter("username") String username) {
        if (StringUtils.isBlank(username)) {
            return FormValidation.error(Messages.GlobalKafkaConfiguration_KafkaSecurityWarning());
        }
        return FormValidation.ok();
    }

    public FormValidation doCheckPassword(@QueryParameter("password") String password) {
        if (StringUtils.isBlank(password)) {
            return FormValidation.error(Messages.GlobalKafkaConfiguration_KafkaSecurityWarning());
        }
        return FormValidation.ok();
    }

    public FormValidation doCheckSslTruststoreLocation(@QueryParameter("sslTruststoreLocation") String sslTruststoreLocation) {
        if (StringUtils.isBlank(sslTruststoreLocation)) {
            return FormValidation.error(Messages.GlobalKafkaConfiguration_KafkaSecurityWarning());
        }
        return FormValidation.ok();
    }

    public FormValidation doCheckSslTruststorePassword(@QueryParameter("sslTruststorePassword") String sslTruststorePassword) {
        if (StringUtils.isBlank(sslTruststorePassword)) {
            return FormValidation.error(Messages.GlobalKafkaConfiguration_KafkaSecurityWarning());
        }
        return FormValidation.ok();
    }

    public FormValidation doCheckSslKeystoreLocation(@QueryParameter("sslKeystoreLocation") String sslKeystoreLocation) {
        if (StringUtils.isBlank(sslKeystoreLocation)) {
            return FormValidation.error(Messages.GlobalKafkaConfiguration_KafkaSecurityWarning());
        }
        return FormValidation.ok();
    }

    public FormValidation doCheckSslKeystorePassword(@QueryParameter("sslKeystorePassword") String sslKeystorePassword) {
        if (StringUtils.isBlank(sslKeystorePassword)) {
            return FormValidation.error(Messages.GlobalKafkaConfiguration_KafkaSecurityWarning());
        }
        return FormValidation.ok();
    }

    public FormValidation doCheckSslKeyPassword(@QueryParameter("sslKeyPassword") String sslKeyPassword) {
        if (StringUtils.isBlank(sslKeyPassword)) {
            return FormValidation.error(Messages.GlobalKafkaConfiguration_KafkaSecurityWarning());
        }
        return FormValidation.ok();
    }

    public FormValidation doTestZookeeperConnection(@QueryParameter("zookeeperURL") final String zookeeperURL)
            throws IOException, ServletException {
        try {
            String[] hostport = zookeeperURL.split(":");
            String host = hostport[0];
            int port = Integer.parseInt(hostport[1]);
            testConnection(host, port);
            return FormValidation.ok("Success");
        } catch (Exception e) {
            return FormValidation.error("Connection error : " + e.getMessage());
        }
    }

    public FormValidation doTestBrokerConnection(@QueryParameter("brokerURL") final String brokerURL)
            throws IOException, ServletException {
        try {
            String[] hostport = brokerURL.split(":");
            String host = hostport[0];
            int port = Integer.parseInt(hostport[1]);
            testConnection(host, port);
            return FormValidation.ok("Success");
        } catch (Exception e) {
            return FormValidation.error("Connection error : " + e.getMessage());
        }
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
        this.brokerURL = formData.getString("brokerURL");
        this.zookeeperURL = formData.getString("zookeeperURL");
        this.username = formData.getString("username");
        this.password = formData.getString("password");
        this.sslTruststoreLocation = formData.getString("sslTruststoreLocation");
        this.sslTruststorePassword = formData.getString("sslTruststorePassword");
        this.sslKeystoreLocation = formData.getString("sslKeystoreLocation");
        this.sslKeystorePassword = formData.getString("sslKeystorePassword");
        this.sslKeyPassword = formData.getString("sslKeyPassword");
        save();
        return super.configure(req, formData);
    }

    private void testConnection(String host, int port) throws IOException {
        new Socket(host, port);
    }
}
