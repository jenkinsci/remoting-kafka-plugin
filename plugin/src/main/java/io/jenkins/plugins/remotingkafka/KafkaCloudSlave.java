package io.jenkins.plugins.remotingkafka;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.slaves.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import jenkins.model.Jenkins;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class KafkaCloudSlave extends AbstractCloudSlave {
    private static final Logger LOGGER = Logger.getLogger(KafkaCloudSlave.class.getName());
    private static final String DEFAULT_AGENT_PREFIX = "remoting-kafka-agent";
    private static final int AGENT_NAME_RANDOM_LENGTH = 5;

    private String cloudName;

    public String getName() {
        return name;
    }

    public String getCloudName() {
        return cloudName;
    }

    public void setCloudName(String cloudName) {
        this.cloudName = cloudName;
    }

    @Nonnull
    public KafkaKubernetesCloud getCloud() {
        return getCloud(cloudName);
    }

    private static KafkaKubernetesCloud getCloud(String cloudName) {
        Cloud cloud = Jenkins.get().getCloud(cloudName);
        if (cloud instanceof KafkaKubernetesCloud) {
            return (KafkaKubernetesCloud) cloud;
        } else {
            throw new IllegalStateException(KafkaCloudSlave.class.getName() + " can be launched only by instances of " + KafkaKubernetesCloud.class.getName() + ". Cloud is " + cloud.getClass().getName());
        }
    }

    public String getNamespace() {
        return getCloud().getNamespace();
    }

    public KafkaCloudSlave(@Nonnull KafkaKubernetesCloud cloud) throws Descriptor.FormException, IOException {
        super(getSlaveName(cloud.name),
                cloud.getDescription(),
                cloud.getWorkingDir(),
                KafkaKubernetesCloud.AGENT_NUM_EXECUTORS,
                cloud.getNodeUsageMode(),
                cloud.getLabel(),
                cloud.isEnableSSL() ?
                        new KafkaComputerLauncher(cloud.getKafkaUsername(), cloud.getSslTruststoreLocation(), cloud.getSslKeystoreLocation())
                        : new KafkaComputerLauncher(),
                // TODO: Retention strat
                CloudSlaveRetentionStrategy.INSTANCE,
                cloud.getNodeProperties() == null ? new ArrayList<>() : cloud.getNodeProperties());

        this.cloudName = cloud.name;
    }

    public static String getSlaveName(String baseNameArg) {
        String baseName = StringUtils.defaultIfBlank(baseNameArg, DEFAULT_AGENT_PREFIX);
        // Because the name is also used in Kubernetes, it should conform to domain name standard
        // No spaces, lower-cased
        baseName = baseName.replaceAll("[ _]", "-").toLowerCase();
        // Keep it under 63 chars (62 is used to account for the '-')
        baseName = baseName.substring(0, Math.min(baseName.length(), 62 - AGENT_NAME_RANDOM_LENGTH));

        Set<String> existingNodeNames = Jenkins.get()
                .getNodes()
                .stream()
                .map(Node::getNodeName)
                .collect(Collectors.toSet());
        while (true) {
            String randString = RandomStringUtils.random(AGENT_NAME_RANDOM_LENGTH, "bcdfghjklmnpqrstvwxz0123456789");
            String name = String.format("%s-%s", baseName, randString);
            if (!existingNodeNames.contains(name)) {
                return name;
            }
        }
    }

    @Override
    public AbstractCloudComputer createComputer() {
        return new KafkaCloudComputer(this);
    }

    @Override
    protected void _terminate(TaskListener listener) throws IOException, InterruptedException {
        LOGGER.info("Terminating Kubernetes instance for agent " + name);
        KafkaKubernetesCloud cloud = getCloud();
        KubernetesClient client = cloud.connect();
        Boolean deleted = client.pods().inNamespace(getNamespace()).withName(name).delete();
        if (!deleted) {
            LOGGER.warning(String.format("Failed to delete pod for agent %s/%s: not found", getNamespace(), name));
        }
    }

    @Extension
    public static final class DescriptorImpl extends SlaveDescriptor {

        @Override
        public String getDisplayName() {
            return "Kafka Cloud Agent";
        }

        @Override
        public boolean isInstantiable() {
            return false;
        }

    }
}
