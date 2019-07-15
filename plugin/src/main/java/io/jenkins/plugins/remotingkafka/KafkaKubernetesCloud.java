package io.jenkins.plugins.remotingkafka;

import hudson.Extension;
import hudson.Util;
import hudson.model.Computer;
import hudson.model.Descriptor;
import hudson.model.Label;
import hudson.model.Node;
import hudson.model.labels.LabelAtom;
import hudson.slaves.Cloud;
import hudson.slaves.NodeProvisioner.PlannedNode;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.CheckForNull;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class KafkaKubernetesCloud extends Cloud {
    private static final Logger LOGGER = Logger.getLogger(KafkaKubernetesCloud.class.getName());
    private static final int AGENT_NUM_EXECUTORS = 1;

    private String jenkinsUrl;
    private String label;

    @DataBoundConstructor
    public KafkaKubernetesCloud(String name) {
        super(name);
    }

    public String getJenkinsUrl() {
        return jenkinsUrl;
    }

    @DataBoundSetter
    public void setJenkinsUrl(String jenkinsUrl) {
        this.jenkinsUrl = jenkinsUrl;
    }

    public String getLabel() {
        return label;
    }

    @DataBoundSetter
    public void setLabel(String label) {
        this.label = label;
    }

    public Set<LabelAtom> getLabelSet() {
        return Label.parse(label);
    }

    public String getNamespace() {
        String ns = GlobalKafkaConfiguration.get().getKubernetesNamespace();
        return StringUtils.isBlank(ns) ? "default" : ns;
    }

    public KubernetesClient connect() {
        GlobalKafkaConfiguration globalConfig = GlobalKafkaConfiguration.get();
        String serverUrl = globalConfig.getKubernetesUrl();
        String namespace = getNamespace();
        String serverCertificate = globalConfig.getKubernetesCertificate();
        String credentialsId = globalConfig.getKubernetesCredentialsId();
        boolean skipTlsVerify = globalConfig.getKubernetesSkipTlsVerify();

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
    }

}
