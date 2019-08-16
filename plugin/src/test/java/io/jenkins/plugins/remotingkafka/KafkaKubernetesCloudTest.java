package io.jenkins.plugins.remotingkafka;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import hudson.model.TaskListener;
import hudson.model.labels.LabelAtom;
import hudson.slaves.NodeProvisioner;
import hudson.util.FormValidation;
import hudson.util.LogTaskListener;
import io.fabric8.kubernetes.api.model.DoneablePod;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KafkaKubernetesCloudTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Rule
    public KubernetesServer k = new KubernetesServer(true, true);

    @Test
    public void testProvisionCreateThenTerminatePod() throws Exception {
        KafkaKubernetesCloud cloud = new KafkaKubernetesCloud("kafka-kubernetes");
        cloud.setServerUrl(k.getMockServer().url("/").toString());
        cloud.setSkipTlsVerify(true);
        j.jenkins.clouds.add(cloud);

        Collection<NodeProvisioner.PlannedNode> provisionedNodes = cloud.provision(new LabelAtom("test"), 1);
        assertThat(provisionedNodes, hasSize(1));
        KafkaCloudSlave slave = (KafkaCloudSlave) provisionedNodes.iterator().next().future.get();
        TaskListener listener = new LogTaskListener(Logger.getLogger(KafkaKubernetesCloudTest.class.getName()), Level.INFO);
        PodResource<Pod, DoneablePod> pod = k.getClient().pods().inNamespace(cloud.getNamespace()).withName(slave.getNodeName());

        assertNull(pod.get());
        j.jenkins.addNode(slave);
        TimeUnit.SECONDS.sleep(20);
        assertNotNull(pod.get());
        slave._terminate(listener);
        assertNull(pod.get());
    }

    @Test
    public void testProvisionWorkloadSize() {
        KafkaKubernetesCloud cloud = new KafkaKubernetesCloud("kafka-kubernetes");
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
                "",
                "",
                "",
                true,
                ""
        );
        assertThat(result.kind, is(FormValidation.Kind.ERROR));

        result = descriptor.doTestConnection(
                k.getMockServer().url("/").toString(),
                "",
                "",
                true,
                ""
        );
        assertThat(result.kind, is(FormValidation.Kind.OK));
    }
}
