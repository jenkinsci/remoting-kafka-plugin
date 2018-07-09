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

import javax.servlet.ServletException;
import java.io.IOException;
import java.net.Socket;
import java.util.Collections;
import java.util.List;

@Extension
public class GlobalKafkaConfiguration extends GlobalConfiguration {
    public static final SchemeRequirement KAFKA_SCHEME = new SchemeRequirement("kafka");

    private String brokerURL;
    private String zookeeperURL;
    private boolean enableSSL;
    private String kafkaCredentialsId;
    private String sslTruststoreCredentialsId;
    private String sslKeystoreCredentialsId;
    private String sslKeyCredentialsId;

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

    public boolean getEnableSSL() {
        return enableSSL;
    }

    public String getKafkaCredentialsId() {
        return kafkaCredentialsId;
    }

    public String getSslTruststoreCredentialsId() {
        return sslTruststoreCredentialsId;
    }

    public String getSslKeystoreCredentialsId() {
        return sslKeystoreCredentialsId;
    }

    public String getSslKeyCredentialsId() {
        return sslKeyCredentialsId;
    }

    public String getKafkaUsername() {
        return getCredential(kafkaCredentialsId).getUsername();
    }

    public String getKafkaPassword() {
        return getCredential(kafkaCredentialsId).getPassword().getPlainText();
    }

    public String getSSLTruststoreLocation() {
        return getCredential(sslTruststoreCredentialsId).getUsername();
    }

    public String getSSLTruststorePassword() {
        return getCredential(sslTruststoreCredentialsId).getPassword().getPlainText();
    }

    public String getSSLKeystoreLocation() {
        return getCredential(sslKeystoreCredentialsId).getUsername();
    }

    public String getSSLKeystorePassword() {
        return getCredential(sslKeystoreCredentialsId).getPassword().getPlainText();
    }

    public String getSSLKeyPassword() {
        return getCredential(sslKeyCredentialsId).getPassword().getPlainText();
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
        this.enableSSL = Boolean.valueOf(formData.getString("enableSSL"));
        save();
        return super.configure(req, formData);
    }

    private void testConnection(String host, int port) throws IOException {
        new Socket(host, port);
    }

    private StandardUsernamePasswordCredentials getCredential(String id) {
        StandardUsernamePasswordCredentials credential = null;

        List<StandardUsernamePasswordCredentials> credentials = CredentialsProvider.lookupCredentials(
                StandardUsernamePasswordCredentials.class, Jenkins.get(), ACL.SYSTEM, Collections.emptyList());

        IdMatcher matcher = new IdMatcher(id);
        for (StandardUsernamePasswordCredentials c : credentials) {
            if (matcher.matches(c)) {
                credential = c;
            }
        }

        return credential;
    }

    public ListBoxModel doFillKafkaCredentialsIdItems(@AncestorInPath Item item, @QueryParameter String credentialsId) {
        StandardListBoxModel result = new StandardListBoxModel();
        if (item == null) {
            if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
                return result.includeCurrentValue(credentialsId);
            }
        }
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

    public FormValidation doCheckKafkaCredentialsId(@AncestorInPath Item item, @QueryParameter String value) {
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
        this.kafkaCredentialsId = value;
        return FormValidation.ok();
    }

    public ListBoxModel doFillSslTruststoreCredentialsIdItems(@AncestorInPath Item item, @QueryParameter String credentialsId) {
        StandardListBoxModel result = new StandardListBoxModel();
        if (item == null) {
            if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
                return result.includeCurrentValue(credentialsId);
            }
        }
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

    public FormValidation doCheckSslTruststoreCredentialsId(@AncestorInPath Item item, @QueryParameter String value) {
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
        this.sslTruststoreCredentialsId = value;
        return FormValidation.ok();
    }

    public ListBoxModel doFillSslKeystoreCredentialsIdItems(@AncestorInPath Item item, @QueryParameter String credentialsId) {
        StandardListBoxModel result = new StandardListBoxModel();
        if (item == null) {
            if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
                return result.includeCurrentValue(credentialsId);
            }
        }
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

    public FormValidation doCheckSslKeystoreCredentialsId(@AncestorInPath Item item, @QueryParameter String value) {
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
        this.sslKeystoreCredentialsId = value;
        return FormValidation.ok();
    }

    public ListBoxModel doFillSslKeyCredentialsIdItems(@AncestorInPath Item item, @QueryParameter String credentialsId) {
        StandardListBoxModel result = new StandardListBoxModel();
        if (item == null) {
            if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
                return result.includeCurrentValue(credentialsId);
            }
        }
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

    public FormValidation doCheckSslKeyCredentialsId(@AncestorInPath Item item, @QueryParameter String value) {
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
        this.sslKeyCredentialsId = value;
        return FormValidation.ok();
    }
}
