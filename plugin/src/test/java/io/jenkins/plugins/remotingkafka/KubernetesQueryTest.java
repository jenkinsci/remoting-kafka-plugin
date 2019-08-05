package io.jenkins.plugins.remotingkafka;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class KubernetesQueryTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Rule
    public KubernetesServer k = new KubernetesServer(true, true);

    @Test
    public void testGetFirstNodePortByServiceName() {
        KubernetesClient client = k.getClient();
        Service svc = new ServiceBuilder()
                .withNewMetadata()
                .withName("kafka-svc")
                .endMetadata()
                .withNewSpec()
                .withType("NodePort")
                .addNewPort()
                .withNodePort(31234)
                .endPort()
                .endSpec()
                .build();
        client.services().create(svc);

        KubernetesQuery query = new KubernetesQuery(client);
        assertThat(query.getFirstNodePortByServiceName("kafka-svc"), is(31234));
    }
}
