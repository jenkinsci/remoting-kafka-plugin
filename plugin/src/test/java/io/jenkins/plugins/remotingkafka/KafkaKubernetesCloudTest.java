package io.jenkins.plugins.remotingkafka;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

import hudson.model.labels.LabelAtom;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class KafkaKubernetesCloudTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

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

}
