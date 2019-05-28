package io.jenkins.plugins.remotingkafka;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Label;
import hudson.slaves.Cloud;
import hudson.slaves.NodeProvisioner;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.Collection;

public class KafkaKubernetesCloud extends Cloud {
    @DataBoundConstructor
    public KafkaKubernetesCloud(String name) {
        super(name);
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<Cloud> {
        @Override
        public String getDisplayName() {
            return "Kafka Kubernetes";
        }
    }

    @Override
    public Collection<NodeProvisioner.PlannedNode> provision(Label label, int i) {
        return null;
    }

    @Override
    public boolean canProvision(Label label) {
        return false;
    }
}
