package io.jenkins.plugins.remotingkafka;

import hudson.Extension;
import hudson.Util;
import hudson.model.Descriptor;
import hudson.model.TaskListener;
import hudson.slaves.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import jenkins.model.Jenkins;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

public class KafkaCloudSlave extends AbstractCloudSlave {
    private static final Logger LOGGER = Logger.getLogger(KafkaCloudSlave.class.getName());
    private static final String DEFAULT_AGENT_PREFIX = "remoting-kafka-agent";

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

    public KafkaCloudSlave(KafkaKubernetesCloud cloud) throws Descriptor.FormException, IOException {
        super(getSlaveName(cloud.name),
                Util.fixNull(cloud.getDescription()),
                cloud.getWorkingDir(),
                KafkaKubernetesCloud.AGENT_NUM_EXECUTORS,
                cloud.getNodeUsageMode(),
                cloud.getLabel(),
                cloud.isEnableSSL() ?
                        new KafkaComputerLauncher(cloud.getKafkaUsername(), cloud.getSslTruststoreLocation(), cloud.getSslKeystoreLocation())
                        : new KafkaComputerLauncher(),
                // TODO: Retention strat
                CloudSlaveRetentionStrategy.INSTANCE,
                cloud.getNodeProperties());

        this.cloudName = cloud.name;
    }

    private static String getSlaveName(String baseName) {
        String randString = RandomStringUtils.random(5, "bcdfghjklmnpqrstvwxz0123456789");
        String name = baseName;
        if (StringUtils.isEmpty(name)) {
            return String.format("%s-%s", DEFAULT_AGENT_PREFIX, randString);
        }
        // Because the name is also used in Kubernetes, it should conform to domain name standard
        // No spaces, lower-cased
        name = name.replaceAll("[ _]", "-").toLowerCase();
        // Keep it under 63 chars (62 is used to account for the '-')
        name = name.substring(0, Math.min(name.length(), 62 - randString.length()));
        return String.format("%s-%s", name, randString);
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
