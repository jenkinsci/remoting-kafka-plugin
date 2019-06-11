package io.jenkins.plugins.remotingkafka;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.SchemeRequirement;
import com.cloudbees.plugins.credentials.matchers.IdMatcher;
import hudson.Extension;
import hudson.Util;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.jenkins.plugins.remotingkafka.exception.RemotingKafkaConfigurationException;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import java.io.IOException;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Extension
@Symbol("kafka")
public class GlobalKafkaConfiguration extends GlobalConfiguration {
    private static final Logger LOGGER = Logger.getLogger(GlobalKafkaConfiguration.class.getName());
    public static final SchemeRequirement KAFKA_SCHEME = new SchemeRequirement("kafka");

    private String brokerURL;
    private String zookeeperURL;
    private boolean enableSSL;
    private String kafkaCredentialsId;
    private String sslTruststoreCredentialsId;
    private String sslKeystoreCredentialsId;
    private String sslKeyCredentialsId;

    private boolean useKubernetes;
    private String kubernetesIp;
    private String kubernetesApiPort;
    private String kubernetesCertificate;
    private String kubernetesCredentialsId;
    private boolean kubernetesSkipTlsVerify;
    private String kubernetesNamespace;

    public GlobalKafkaConfiguration() {
        load();
    }

    public static GlobalKafkaConfiguration get() {
        return GlobalConfiguration.all().getInstance(GlobalKafkaConfiguration.class);
    }

    public String getBrokerURL() {
        return brokerURL;
    }

    public void setBrokerURL(String brokerURL) {
        this.brokerURL = brokerURL;
    }

    public String getZookeeperURL() {
        return zookeeperURL;
    }

    public void setZookeeperURL(String zookeeperURL) {
        this.zookeeperURL = zookeeperURL;
    }

    public boolean getEnableSSL() {
        return enableSSL;
    }

    public void setEnableSSL(boolean enableSSL) {
        this.enableSSL = enableSSL;
    }

    public String getKafkaCredentialsId() {
        return kafkaCredentialsId;
    }

    public void setKafkaCredentialsId(String kafkaCredentialsId) {
        this.kafkaCredentialsId = kafkaCredentialsId;
    }

    public String getSslTruststoreCredentialsId() {
        return sslTruststoreCredentialsId;
    }

    public void setSslTruststoreCredentialsId(String sslTruststoreCredentialsId) {
        this.sslTruststoreCredentialsId = sslTruststoreCredentialsId;
    }

    public String getSslKeystoreCredentialsId() {
        return sslKeystoreCredentialsId;
    }

    public void setSslKeystoreCredentialsId(String sslKeystoreCredentialsId) {
        this.sslKeystoreCredentialsId = sslKeystoreCredentialsId;
    }

    public String getSslKeyCredentialsId() {
        return sslKeyCredentialsId;
    }

    public void setSslKeyCredentialsId(String sslKeyCredentialsId) {
        this.sslKeyCredentialsId = sslKeyCredentialsId;
    }

    public boolean getUseKubernetes() {
        return useKubernetes;
    }

    public void setUseKubernetes(boolean useKubernetes) {
        this.useKubernetes = useKubernetes;
    }

    public String getKubernetesIp() {
        return kubernetesIp;
    }

    public void setKubernetesIp(String kubernetesIp) {
        this.kubernetesIp = kubernetesIp;
    }

    public String getKubernetesApiPort() {
        return kubernetesApiPort;
    }

    public void setKubernetesApiPort(String kubernetesApiPort) {
        this.kubernetesApiPort = kubernetesApiPort;
    }

    public String getKubernetesCertificate() {
        return kubernetesCertificate;
    }

    public void setKubernetesCertificate(String kubernetesCertificate) {
        this.kubernetesCertificate = kubernetesCertificate;
    }

    public String getKubernetesCredentialsId() {
        return kubernetesCredentialsId;
    }

    public void setKubernetesCredentialsId(String kubernetesCredentialsId) {
        this.kubernetesCredentialsId = kubernetesCredentialsId;
    }

    public boolean getKubernetesSkipTlsVerify() {
        return kubernetesSkipTlsVerify;
    }

    public void setKubernetesSkipTlsVerify(boolean kubernetesSkipTlsVerify) {
        this.kubernetesSkipTlsVerify = kubernetesSkipTlsVerify;
    }

    public String getKubernetesNamespace() {
        return kubernetesNamespace;
    }

    public void setKubernetesNamespace(String kubernetesNamespace) {
        this.kubernetesNamespace = kubernetesNamespace;
    }

    public String getKafkaUsername() throws RemotingKafkaConfigurationException {
        StandardUsernamePasswordCredentials credential = getCredential(kafkaCredentialsId);
        if (credential == null) {
            throw new RemotingKafkaConfigurationException("No Kafka credential provided");
        }
        return credential.getUsername();
    }

    public String getKafkaPassword() throws RemotingKafkaConfigurationException {
        StandardUsernamePasswordCredentials credential = getCredential(kafkaCredentialsId);
        if (credential == null) {
            throw new RemotingKafkaConfigurationException("No Kafka credential provided");
        }
        return credential.getPassword().getPlainText();
    }

    public String getSSLTruststoreLocation() throws RemotingKafkaConfigurationException {
        StandardUsernamePasswordCredentials credential = getCredential(sslTruststoreCredentialsId);
        if (credential == null) {
            throw new RemotingKafkaConfigurationException("No SSL truststore credential provided");
        }
        return credential.getUsername();
    }

    public String getSSLTruststorePassword() throws RemotingKafkaConfigurationException {
        StandardUsernamePasswordCredentials credential = getCredential(sslTruststoreCredentialsId);
        if (credential == null) {
            throw new RemotingKafkaConfigurationException("No SSL truststore credential provided");
        }
        return credential.getPassword().getPlainText();
    }

    public String getSSLKeystoreLocation() throws RemotingKafkaConfigurationException {
        StandardUsernamePasswordCredentials credential = getCredential(sslKeystoreCredentialsId);
        if (credential == null) {
            throw new RemotingKafkaConfigurationException("No SSL keystore credential provided");
        }
        return credential.getUsername();
    }

    public String getSSLKeystorePassword() throws RemotingKafkaConfigurationException {
        StandardUsernamePasswordCredentials credential = getCredential(sslKeystoreCredentialsId);
        if (credential == null) {
            throw new RemotingKafkaConfigurationException("No SSL keystore credential provided");
        }
        return credential.getPassword().getPlainText();
    }

    public String getSSLKeyPassword() throws RemotingKafkaConfigurationException {
        StandardUsernamePasswordCredentials credential = getCredential(sslKeyCredentialsId);
        if (credential == null) {
            throw new RemotingKafkaConfigurationException("No SSL key credential provided");
        }
        return credential.getPassword().getPlainText();
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

    @RequirePOST
    public FormValidation doTestZookeeperConnection(@QueryParameter("zookeeperURL") final String zookeeperURL)
            throws IOException, ServletException {
        if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
            return FormValidation.error("Need admin permission to perform this action");
        }
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

    @RequirePOST
    public FormValidation doTestBrokerConnection(@QueryParameter("brokerURL") final String brokerURL)
            throws IOException, ServletException {
        if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
            return FormValidation.error("Need admin permission to perform this action");
        }
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

    @RequirePOST
    public FormValidation doTestKubernetesConnection(
            @QueryParameter("kubernetesIp") String serverIp,
            @QueryParameter("kubernetesApiPort") String serverPort,
            @QueryParameter("kubernetesCredentialsId") String credentialsId,
            @QueryParameter("kubernetesCertificate") String serverCertificate,
            @QueryParameter("kubernetesSkipTlsVerify") boolean skipTlsVerify,
            @QueryParameter("kubernetesNamespace") String namespace
    ) {
        Jenkins.get().checkPermission(Jenkins.ADMINISTER);

        try {
            String serverUrl = new URIBuilder()
                    .setHost(serverIp)
                    .setPort(Integer.parseInt(serverPort))
                    .toString();
            KubernetesClient client = new KubernetesFactoryAdapter(serverUrl, namespace,
                    Util.fixEmpty(serverCertificate), Util.fixEmpty(credentialsId), skipTlsVerify
            ).createClient();
            // Call Pod list API to ensure functionality
            client.pods().list();
            return FormValidation.ok("Success");
        } catch (KubernetesClientException e) {
            LOGGER.log(Level.FINE, "Error testing Kubernetes connection", e);
            return FormValidation.error("Error: %s", e.getCause() == null
                    ? e.getMessage()
                    : String.format("%s: %s", e.getCause().getClass().getName(), e.getCause().getMessage()));
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Error testing Kubernetes connection", e);
            return FormValidation.error("Error: %s", e.getMessage());
        }
    }

    @RequirePOST
    public FormValidation doStartKafkaOnKubernetes(
            @QueryParameter("kubernetesIp") String serverIp,
            @QueryParameter("kubernetesApiPort") String serverPort,
            @QueryParameter("kubernetesCredentialsId") String credentialsId,
            @QueryParameter("kubernetesCertificate") String serverCertificate,
            @QueryParameter("kubernetesSkipTlsVerify") boolean skipTlsVerify,
            @QueryParameter("kubernetesNamespace") String namespace
    ) {
        Jenkins.get().checkPermission(Jenkins.ADMINISTER);

        try {
            String serverUrl = new URIBuilder()
                    .setHost(serverIp)
                    .setPort(Integer.parseInt(serverPort))
                    .toString();
            KubernetesClient client = new KubernetesFactoryAdapter(serverUrl, namespace,
                    Util.fixEmpty(serverCertificate), Util.fixEmpty(credentialsId), skipTlsVerify
            ).createClient();
            Class clazz = GlobalKafkaConfiguration.class;
            client.load(clazz.getResourceAsStream("kubernetes/zookeeper.yaml")).createOrReplace();
            LOGGER.info("Starting Zookeeper");
            client.load(clazz.getResourceAsStream("kubernetes/kafka-service.yaml")).createOrReplace();
            LOGGER.info("Starting Kafka Services");
            Integer zookeeperPort = KubernetesUtils.getFirstNodePortByServiceName(client, "zookeeper-svc");
            Integer kafkaPort = KubernetesUtils.getFirstNodePortByServiceName(client, "kafka-svc");

            // Set Kafka advertised.listeners property
            List<HasMetadata> kafkaStatefulSetResources =
                    client.load(clazz.getResourceAsStream("kubernetes/kafka-statefulset.yaml")).get();
            StatefulSet kafkaStatefulSet = (StatefulSet) kafkaStatefulSetResources.
                    stream().
                    filter(res -> res.getKind().equals("StatefulSet") && res.getMetadata().getName().equals("kafka")).
                    findFirst().
                    orElse(null);
            if (kafkaStatefulSet == null)
                throw new RemotingKafkaConfigurationException("Couldn't find StatefulSet named kafka in YAML configuration");
            Container kafkaContainer = kafkaStatefulSet.
                    getSpec().
                    getTemplate().
                    getSpec().
                    getContainers().
                    stream().
                    filter(con -> con.getName().equals("kafka")).
                    findFirst().
                    orElse(null);
            if (kafkaContainer == null)
                throw new RemotingKafkaConfigurationException("Couldn't find Container named kafka in template specification");
            kafkaContainer.getEnv().add(new EnvVar(
                    "KAFKA_ADVERTISED_LISTENERS",
                    String.format("EXTERNAL://%s:%s", serverIp, kafkaPort),
                    null
            ));
            client.resourceList(kafkaStatefulSetResources).createOrReplace();
            LOGGER.info("Starting Kafka StatefulSet");

            // Probe for Kafka readiness
            while (true) {
                try {
                    testConnection(serverIp, kafkaPort);
                    break;
                } catch (IOException e) {
                    LOGGER.info(String.format("Waiting for Kafka connection at %s:%s", serverIp, kafkaPort));
                }
                Thread.sleep(2000);
            }
            LOGGER.info("Zookeeper and Kafka started");

            // Set Zookeeper and Broker URL
            GlobalKafkaConfiguration.get().setZookeeperURL(serverIp + ":" + zookeeperPort);
            GlobalKafkaConfiguration.get().setBrokerURL(serverIp + ":" + kafkaPort);
            return FormValidation.ok(String.format("Success. Zookeeper: %s:%s and Kafka: %s:%s", serverIp, zookeeperPort, serverIp, kafkaPort));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error", e);
            return FormValidation.error("Error: %s", e.getCause() == null
                    ? e.getMessage()
                    : String.format("%s: %s", e.getCause().getClass().getName(), e.getCause().getMessage()));
        }
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
        this.brokerURL = formData.getString("brokerURL");
        this.zookeeperURL = formData.getString("zookeeperURL");
        this.enableSSL = Boolean.valueOf(formData.getString("enableSSL"));

        this.useKubernetes = Boolean.valueOf(formData.getString("useKubernetes"));
        this.kubernetesIp = formData.getString("kubernetesIp");
        this.kubernetesApiPort = formData.getString("kubernetesApiPort");
        this.kubernetesCertificate = formData.getString("kubernetesCertificate");
        this.kubernetesSkipTlsVerify = Boolean.valueOf(formData.getString("kubernetesSkipTlsVerify"));
        this.kubernetesNamespace = formData.getString("kubernetesNamespace");
        save();
        return true;
    }

    private void testConnection(String host, int port) throws IOException {
        new Socket(host, port);
    }

    @CheckForNull
    private StandardUsernamePasswordCredentials getCredential(@Nonnull String id) {
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

    @RequirePOST
    public ListBoxModel doFillKafkaCredentialsIdItems(@AncestorInPath Item item, @QueryParameter String credentialsId) {
        return fillCredentialsIdItems(item, credentialsId);
    }

    public FormValidation doCheckKafkaCredentialsId(@AncestorInPath Item item, @QueryParameter String value) {
        FormValidation checkedResult = checkCredentialsId(item, value);
        boolean isAdminOrNullItem = (item != null || Jenkins.get().hasPermission(Jenkins.ADMINISTER));
        if (isAdminOrNullItem && checkedResult.equals(FormValidation.ok())) {
            this.kafkaCredentialsId = value;
        }
        return checkedResult;
    }

    @RequirePOST
    public ListBoxModel doFillSslTruststoreCredentialsIdItems(@AncestorInPath Item item, @QueryParameter String credentialsId) {
        return fillCredentialsIdItems(item, credentialsId);
    }

    public FormValidation doCheckSslTruststoreCredentialsId(@AncestorInPath Item item, @QueryParameter String value) {
        FormValidation checkedResult = checkCredentialsId(item, value);
        boolean isAdminOrNullItem = (item != null || Jenkins.get().hasPermission(Jenkins.ADMINISTER));
        if (isAdminOrNullItem && checkedResult.equals(FormValidation.ok())) {
            this.sslTruststoreCredentialsId = value;
        }
        return checkedResult;
    }

    @RequirePOST
    public ListBoxModel doFillSslKeystoreCredentialsIdItems(@AncestorInPath Item item, @QueryParameter String credentialsId) {
        return fillCredentialsIdItems(item, credentialsId);
    }

    public FormValidation doCheckSslKeystoreCredentialsId(@AncestorInPath Item item, @QueryParameter String value) {
        FormValidation checkedResult = checkCredentialsId(item, value);
        boolean isAdminOrNullItem = (item != null || Jenkins.get().hasPermission(Jenkins.ADMINISTER));
        if (isAdminOrNullItem && checkedResult.equals(FormValidation.ok())) {
            this.sslKeystoreCredentialsId = value;
        }
        return checkedResult;
    }

    @RequirePOST
    public ListBoxModel doFillSslKeyCredentialsIdItems(@AncestorInPath Item item, @QueryParameter String credentialsId) {
        return fillCredentialsIdItems(item, credentialsId);
    }

    public FormValidation doCheckSslKeyCredentialsId(@AncestorInPath Item item, @QueryParameter String value) {
        FormValidation checkedResult = checkCredentialsId(item, value);
        boolean isAdminOrNullItem = (item != null || Jenkins.get().hasPermission(Jenkins.ADMINISTER));
        if (isAdminOrNullItem && checkedResult.equals(FormValidation.ok())) {
            this.sslKeyCredentialsId = value;
        }
        return checkedResult;
    }

    @RequirePOST
    public ListBoxModel doFillKubernetesCredentialsIdItems(@AncestorInPath Item item, @QueryParameter String credentialsId) {
        return fillCredentialsIdItems(item, credentialsId);
    }

    public FormValidation doCheckKubernetesCredentialsId(@AncestorInPath Item item, @QueryParameter String value) {
        FormValidation checkedResult = checkCredentialsId(item, value);
        boolean isAdminOrNullItem = (item != null || Jenkins.get().hasPermission(Jenkins.ADMINISTER));
        if (isAdminOrNullItem && checkedResult.equals(FormValidation.ok())) {
            this.kubernetesCredentialsId = value;
        }
        return checkedResult;
    }

    private FormValidation checkCredentialsId(@AncestorInPath Item item, @QueryParameter String value) {
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

    private ListBoxModel fillCredentialsIdItems(@AncestorInPath Item item, @QueryParameter String credentialsId) {
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
}
