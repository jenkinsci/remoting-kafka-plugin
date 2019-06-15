package io.jenkins.plugins.remotingkafka;

import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.client.KubernetesClient;

import javax.annotation.CheckForNull;
import java.util.List;

public class KubernetesQuery {
    private KubernetesClient client;

    public KubernetesQuery(KubernetesClient client) {
        this.client = client;
    }

    @CheckForNull
    public Integer getFirstNodePortByServiceName(String name) {
        List<ServicePort> ports = client.services().withName(name).get().getSpec().getPorts();
        if (ports.size() == 0) return null;
        else return ports.get(0).getNodePort();
    }
}
