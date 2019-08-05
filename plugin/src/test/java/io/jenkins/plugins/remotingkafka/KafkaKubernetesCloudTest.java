package io.jenkins.plugins.remotingkafka;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;

import hudson.model.FreeStyleProject;
import hudson.model.labels.LabelAtom;
import hudson.slaves.NodeProvisioner;
import hudson.util.FormValidation;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;
import java.util.Collection;

public class KafkaKubernetesCloudTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Rule
    public KubernetesServer k = new KubernetesServer(true, true);

    @Test
    public void testProvision() {
        KafkaKubernetesCloud cloud = new KafkaKubernetesCloud("kafka-kubernetes");
        cloud.setServerUrl(k.getMockServer().url("/").toString());
        Collection<NodeProvisioner.PlannedNode> nodes = cloud.provision(new LabelAtom("test"), 200);
        assertThat(nodes, hasSize(200));
    }

    @Test
    public void testCanProvisionSingleLabel() {
        KafkaKubernetesCloud cloud = new KafkaKubernetesCloud("kafka-kubernetes");
        cloud.setLabel("test");
        assertThat(cloud.canProvision(null), is(false));
        assertThat(cloud.canProvision(new LabelAtom("test")), is(true));
        assertThat(cloud.canProvision(new LabelAtom("wrong")), is(false));
    }

    @Test
    public void testCanProvisionMultipleLabels() {
        KafkaKubernetesCloud cloud = new KafkaKubernetesCloud("kafka-kubernetes");
        cloud.setLabel("label1 label2");
        assertThat(cloud.canProvision(new LabelAtom("label1")), is(true));
        assertThat(cloud.canProvision(new LabelAtom("label3")), is(false));
    }

    @Test
    public void testGetNamespaceDefaultValue() {
        KafkaKubernetesCloud cloud = new KafkaKubernetesCloud("kafka-kubernetes");
        cloud.setNamespace(null);
        assertThat(cloud.getNamespace(), is("default"));
        cloud.setNamespace("");
        assertThat(cloud.getNamespace(), is("default"));
    }

    @Test
    public void testTestKubernetesConnection() {
        KafkaKubernetesCloud.DescriptorImpl descriptor = new KafkaKubernetesCloud.DescriptorImpl();
        FormValidation result = descriptor.doTestConnection(
                k.getMockServer().url("/").toString(),
                "",
                "",
                true,
                ""
        );
        assertThat(result.kind, is(FormValidation.Kind.OK));
    }
}
