package io.jenkins.plugins.remotingkafka;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCertificateCredentials;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import hudson.Extension;
import hudson.Util;
import hudson.model.Computer;
import hudson.model.Descriptor;
import hudson.model.Label;
import hudson.model.Node;
import hudson.model.labels.LabelAtom;
import hudson.security.ACL;
import hudson.slaves.Cloud;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodeProvisioner.PlannedNode;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.kubernetes.credentials.TokenProducer;
import org.jenkinsci.plugins.plaincredentials.FileCredentials;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.annotation.CheckForNull;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class KafkaKubernetesCloud extends Cloud {
    private static final Logger LOGGER = Logger.getLogger(KafkaKubernetesCloud.class.getName());
    public static final int AGENT_NUM_EXECUTORS = 1;

    private String serverUrl;
    @CheckForNull
    private String serverCertificate;
    private String credentialsId;
    private boolean skipTlsVerify;
    private String namespace;

    private String jenkinsUrl;
    private String containerImage;
    private String idleMinutes;
    private String label;
    private Node.Mode nodeUsageMode;
    private String description;
    private String workingDir;
    private List<? extends NodeProperty<?>> nodeProperties;

    private String kafkaUsername;
    private String sslTruststoreLocation;
    private String sslKeystoreLocation;
    private boolean enableSSL = false;

    @DataBoundConstructor
    public KafkaKubernetesCloud(String name) {
        super(name);
    }

    public String getServerUrl() {
        return serverUrl;
    }

    @DataBoundSetter
    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getServerCertificate() {
        return serverCertificate;
    }

    @DataBoundSetter
    public void setServerCertificate(String serverCertificate) {
        this.serverCertificate = serverCertificate;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    @DataBoundSetter
    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    public boolean isSkipTlsVerify() {
        return skipTlsVerify;
    }

    @DataBoundSetter
    public void setSkipTlsVerify(boolean skipTlsVerify) {
        this.skipTlsVerify = skipTlsVerify;
    }

    public String getNamespace() {
        return StringUtils.defaultIfBlank(namespace, "default");
    }

    @DataBoundSetter
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getJenkinsUrl() {
        return jenkinsUrl;
    }

    @DataBoundSetter
    public void setJenkinsUrl(String jenkinsUrl) {
        this.jenkinsUrl = jenkinsUrl;
    }

    public String getContainerImage() {
        return containerImage;
    }

    @DataBoundSetter
    public void setContainerImage(String containerImage) {
        this.containerImage = containerImage;
    }

    public String getIdleMinutes() {
        return StringUtils.defaultIfBlank(idleMinutes, "0");
    }

    @DataBoundSetter
    public void setIdleMinutes(String idleMinutes) {
        this.idleMinutes = idleMinutes;
    }

    public String getLabel() {
        return label;
    }

    @DataBoundSetter
    public void setLabel(String label) {
        this.label = label;
    }

    public Node.Mode getNodeUsageMode() {
        return nodeUsageMode;
    }

    @DataBoundSetter
    public void setNodeUsageMode(Node.Mode nodeUsageMode) {
        this.nodeUsageMode = nodeUsageMode;
    }

    public String getDescription() {
        return description;
    }

    @DataBoundSetter
    public void setDescription(String description) {
        this.description = description;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    @DataBoundSetter
    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    public List<? extends NodeProperty<?>> getNodeProperties() {
        return nodeProperties;
    }

    @DataBoundSetter
    public void setNodeProperties(List<? extends NodeProperty<?>> nodeProperties) {
        this.nodeProperties = nodeProperties;
    }

    public String getKafkaUsername() {
        return kafkaUsername;
    }

    @DataBoundSetter
    public void setKafkaUsername(String kafkaUsername) {
        this.kafkaUsername = kafkaUsername;
    }

    public String getSslTruststoreLocation() {
        return sslTruststoreLocation;
    }

    @DataBoundSetter
    public void setSslTruststoreLocation(String sslTruststoreLocation) {
        this.sslTruststoreLocation = sslTruststoreLocation;
    }

    public String getSslKeystoreLocation() {
        return sslKeystoreLocation;
    }

    @DataBoundSetter
    public void setSslKeystoreLocation(String sslKeystoreLocation) {
        this.sslKeystoreLocation = sslKeystoreLocation;
    }

    public boolean isEnableSSL() {
        return enableSSL;
    }

    @DataBoundSetter
    public void setEnableSSL(boolean enableSSL) {
        this.enableSSL = enableSSL;
    }

    public Set<LabelAtom> getLabelSet() {
        return Label.parse(label);
    }

    public KubernetesClient connect() {
        try (KubernetesClient client = new KubernetesFactoryAdapter(serverUrl, namespace,
                Util.fixEmpty(serverCertificate), Util.fixEmpty(credentialsId), skipTlsVerify
        ).createClient()) {
            return client;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.warning("Error connecting to Kubernetes client from Cloud " + name);
            return null;
        }
    }

    @Override
    public Collection<PlannedNode> provision(Label label, int excessWorkload) {
        Set<String> allInProvisioning = getNodesInProvisioning(label);
        LOGGER.info("In provisioning : " + allInProvisioning);
        int toBeProvisioned = Math.max(0, excessWorkload - allInProvisioning.size());
        LOGGER.info("Excess workload after pending Kubernetes agents: " + toBeProvisioned);

        List<PlannedNode> provisionNodes = new ArrayList<>();
        for (int i = 0; i < toBeProvisioned; i++) {
            PlannedNode node = new PlannedNode(name,
                    Computer.threadPoolForRemoting.submit(() -> new KafkaCloudSlave(this)),
                    AGENT_NUM_EXECUTORS);
            provisionNodes.add(node);
        }
        return provisionNodes;
    }

    public Set<String> getNodesInProvisioning(@CheckForNull Label label) {
        if (label == null) return Collections.emptySet();
        return label.getNodes().stream()
                .filter(KafkaCloudSlave.class::isInstance)
                .filter(node -> {
                    Computer computer = node.toComputer();
                    return computer != null && !computer.isOnline();
                })
                .map(Node::getNodeName)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean canProvision(@CheckForNull Label label) {
        if (label == null) return false;
        return label.matches(getLabelSet());
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<Cloud> {
        @Override
        public String getDisplayName() {
            return "Kafka Kubernetes";
        }

        @RequirePOST
        public FormValidation doTestConnection(
                @QueryParameter("serverUrl") String serverUrl,
                @QueryParameter("credentialsId") String credentialsId,
                @QueryParameter("serverCertificate") String serverCertificate,
                @QueryParameter("skipTlsVerify") boolean skipTlsVerify,
                @QueryParameter("namespace") String namespace
        ) {
            Jenkins.get().checkPermission(Jenkins.ADMINISTER);

            try {
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
        public ListBoxModel doFillCredentialsIdItems(@QueryParameter String serverUrl) {
            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            return new StandardListBoxModel().withEmptySelection()
                    .withMatching(
                            CredentialsMatchers.anyOf(
                                    CredentialsMatchers.instanceOf(StandardUsernamePasswordCredentials.class),
                                    CredentialsMatchers.instanceOf(FileCredentials.class),
                                    CredentialsMatchers.instanceOf(TokenProducer.class),
                                    CredentialsMatchers.instanceOf(StandardCertificateCredentials.class),
                                    CredentialsMatchers.instanceOf(StringCredentials.class)),
                            CredentialsProvider.lookupCredentials(StandardCredentials.class,
                                    Jenkins.get(),
                                    ACL.SYSTEM,
                                    serverUrl != null ? URIRequirementBuilder.fromUri(serverUrl).build()
                                            : Collections.EMPTY_LIST
                            ));
        }
    }

}
