package io.jenkins.plugins.remotingkafka;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import hudson.util.FormValidation;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class GlobalKafkaConfigurationTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Rule
    public KubernetesServer k = new KubernetesServer(true, true);

    @Test
    public void configRoundTrip() throws Exception {
        JenkinsRule.WebClient web = j.createWebClient();
        HtmlForm form = web.goTo("configure").getFormByName("config");
        j.submit(form);

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
        g.setKubernetesCredentialsId("dummy");
        g.setKubernetesSkipTlsVerify(false);
        g.setKubernetesNamespace("default");
        g.save();
        g.load();

        form = web.goTo("configure").getFormByName("config");
        j.submit(form);
    }

    @Test
    public void testTestZookeeperConnection() throws Exception {
        GlobalKafkaConfiguration g = GlobalKafkaConfiguration.get();
        FormValidation result = g.doTestZookeeperConnection("");
        assertThat(result.kind, is(FormValidation.Kind.ERROR));

        try (MockWebServer server = new MockWebServer()) {
            server.start();
            result = g.doTestZookeeperConnection(server.getHostName() + ":" + server.getPort());
            assertThat(result.kind, is(FormValidation.Kind.OK));
        }
    }

    @Test
    public void testTestKafkaConnection() throws Exception {
        GlobalKafkaConfiguration g = GlobalKafkaConfiguration.get();
        FormValidation result = g.doTestBrokerConnection("");
        assertThat(result.kind, is(FormValidation.Kind.ERROR));

        try (MockWebServer server = new MockWebServer()) {
            server.start();
            result = g.doTestBrokerConnection(server.getHostName() + ":" + server.getPort());
            assertThat(result.kind, is(FormValidation.Kind.OK));
        }
    }

    @Test
    public void testTestKubernetesConnection() {
        GlobalKafkaConfiguration g = GlobalKafkaConfiguration.get();
        FormValidation result = g.doTestKubernetesConnection(
                "",
                "",
                "",
                "",
                false,
                ""
        );
        assertThat(result.kind, is(FormValidation.Kind.ERROR));

        result = g.doTestKubernetesConnection(
                k.getMockServer().getHostName(),
                String.valueOf(k.getMockServer().getPort()),
                "",
                "",
                true,
                ""
        );
        assertThat(result.kind, is(FormValidation.Kind.OK));
    }

    @Test
    public void testStartKafkaOnKubernetes() throws Exception {
        GlobalKafkaConfiguration g = GlobalKafkaConfiguration.get();
        FormValidation result = g.doStartKafkaOnKubernetes(
                "",
                "",
                "",
                "",
                true,
                ""
        );
        assertThat(result.kind, is(FormValidation.Kind.ERROR));

        try (MockWebServer kafkaServer = new MockWebServer();
             MockWebServer zookeeperServer = new MockWebServer()) {
            kafkaServer.start(30092);
            zookeeperServer.start(32181);
            result = g.doStartKafkaOnKubernetes(
                    k.getMockServer().getHostName(),
                    String.valueOf(k.getMockServer().getPort()),
                    "",
                    "",
                    true,
                    ""
            );
            assertThat(result.kind, is(FormValidation.Kind.OK));
            assertThat(g.doTestBrokerConnection(g.getBrokerURL()).kind, is(FormValidation.Kind.OK));
            assertThat(g.doTestZookeeperConnection(g.getZookeeperURL()).kind, is(FormValidation.Kind.OK));
        }
    }
}
