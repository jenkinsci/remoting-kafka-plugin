package io.jenkins.plugins.remotingkafka;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class GlobalKafkaConfigurationTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void configRoundTrip() {
        GlobalKafkaConfiguration g = GlobalKafkaConfiguration.get();
        g.setBrokerURL("172.17.0.1:9092");
        g.setZookeeperURL("172.17.0.1:2181");
        g.setEnableSSL(true);
        g.setKafkaCredentialsId("dummy");
        g.setSslKeyCredentialsId("dummy");
        g.setSslKeystoreCredentialsId("dummy");
        g.setSslTruststoreCredentialsId("dummy");
        g.setUseKubernetes(true);
        g.setKubernetesIp("192.168.99.100");
        g.setKubernetesApiPort("8443");
        g.setKubernetesCertificate("dummy");
        g.setKafkaCredentialsId("dummy");
        g.setKubernetesSkipTlsVerify(false);
        g.setKubernetesNamespace("default");
        g.save();
        g.load();
    }
}
