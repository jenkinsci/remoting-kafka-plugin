package io.jenkins.plugins.remotingkafka;

import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.util.List;

public class KubernetesUtils {
    public static Integer getFirstNodePortByServiceName(KubernetesClient client, String name) {
        List<ServicePort> ports = client.services().withName(name).get().getSpec().getPorts();
        if (ports.size() == 0) return null;
        else return ports.get(0).getNodePort();
    }
}
