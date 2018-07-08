package io.jenkins.plugins.remotingkafka;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.SchemeRequirement;
import com.cloudbees.plugins.credentials.matchers.IdMatcher;
import hudson.Extension;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.servlet.ServletException;
import java.io.IOException;
import java.net.Socket;
import java.util.Collections;
import java.util.List;

@Extension
public class GlobalKafkaConfiguration extends GlobalConfiguration {
    public static final SchemeRequirement KAFKA_SCHEME = new SchemeRequirement("kafka");
    private String credentialsId;
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

    public String getCredentialsId() {
        return credentialsId;
    }

    @RequirePOST
    public FormValidation doCheckBrokerURL(@QueryParameter("brokerURL") final String brokerURL) {
        if (StringUtils.isBlank(brokerURL)) {
            return FormValidation.error(Messages.GlobalKafkaConfiguration_KafkaConnectionURLWarning());
        }
        return FormValidation.ok();
    }

    @RequirePOST
    public FormValidation doCheckZookeeperURL(@QueryParameter("zookeeperURL") String zookeeperURL) {
        if (StringUtils.isBlank(zookeeperURL)) {
            return FormValidation.error(Messages.GlobalKafkaConfiguration_ZookeeperURLWarning());
        }
        return FormValidation.ok();
    }

    @RequirePOST
    public FormValidation doCheckSslTruststoreLocation(@QueryParameter("sslTruststoreLocation") String sslTruststoreLocation) {
        if (StringUtils.isBlank(sslTruststoreLocation)) {
            return FormValidation.error(Messages.GlobalKafkaConfiguration_KafkaSecurityWarning());
        }
        return FormValidation.ok();
    }

    @RequirePOST
    public FormValidation doCheckSslTruststorePassword(@QueryParameter("sslTruststorePassword") String sslTruststorePassword) {
        if (StringUtils.isBlank(sslTruststorePassword)) {
            return FormValidation.error(Messages.GlobalKafkaConfiguration_KafkaSecurityWarning());
        }
        return FormValidation.ok();
    }

    @RequirePOST
    public FormValidation doCheckSslKeystoreLocation(@QueryParameter("sslKeystoreLocation") String sslKeystoreLocation) {
        if (StringUtils.isBlank(sslKeystoreLocation)) {
            return FormValidation.error(Messages.GlobalKafkaConfiguration_KafkaSecurityWarning());
        }
        return FormValidation.ok();
    }

    @RequirePOST
    public FormValidation doCheckSslKeystorePassword(@QueryParameter("sslKeystorePassword") String sslKeystorePassword) {
        if (StringUtils.isBlank(sslKeystorePassword)) {
            return FormValidation.error(Messages.GlobalKafkaConfiguration_KafkaSecurityWarning());
        }
        return FormValidation.ok();
    }

    @RequirePOST
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
        StandardUsernamePasswordCredentials credential = getCredential();
        this.username = credential.getUsername();
        this.password = credential.getPassword().getPlainText();
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

    private StandardUsernamePasswordCredentials getCredential() {
        StandardUsernamePasswordCredentials credential = null;

        List<StandardUsernamePasswordCredentials> credentials = CredentialsProvider.lookupCredentials(
                StandardUsernamePasswordCredentials.class, Jenkins.get(), ACL.SYSTEM, Collections.emptyList());

        IdMatcher matcher = new IdMatcher(credentialsId);
        for (StandardUsernamePasswordCredentials c : credentials) {
            if (matcher.matches(c)) {
                credential = c;
            }
        }

        return credential;
    }

    public ListBoxModel doFillCredentialsIdItems(@AncestorInPath Item item, @QueryParameter String credentialsId) {
        StandardListBoxModel result = new StandardListBoxModel();
        if (item == null) {
            if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
                return result.includeCurrentValue(credentialsId);
            }
        }
        this.credentialsId = credentialsId;
        return result
                .includeMatchingAs(
                        ACL.SYSTEM,
                        Jenkins.get(),
                        StandardUsernamePasswordCredentials.class,
                        Collections.singletonList(KAFKA_SCHEME),
                        CredentialsMatchers.always()
                )
                .includeCurrentValue(credentialsId);
    }

    @RequirePOST
    public FormValidation doCheckCredentialsId(@AncestorInPath Item item, @QueryParameter String value) {
        if (item == null) {
            if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
                return FormValidation.ok();
            }
        }
        if (value.startsWith("${") && value.endsWith("}")) {
            return FormValidation.warning("Cannot validate expression based credentials");
        }
        if (CredentialsProvider.listCredentials(
                StandardUsernamePasswordCredentials.class,
                Jenkins.get(),
                ACL.SYSTEM,
                Collections.singletonList(KAFKA_SCHEME),
                CredentialsMatchers.withId(value)
        ).isEmpty()) {
            return FormValidation.error("Cannot find currently selected credentials");
        }
        return FormValidation.ok();
    }
}
