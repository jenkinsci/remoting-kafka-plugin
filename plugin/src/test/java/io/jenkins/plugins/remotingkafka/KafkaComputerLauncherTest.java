package io.jenkins.plugins.remotingkafka;

import hudson.model.Computer;
import hudson.model.FreeStyleProject;
import hudson.slaves.DumbSlave;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.testcontainers.containers.KafkaContainer;

import static org.junit.Assert.assertNotNull;

public class KafkaComputerLauncherTest {
    @ClassRule
    public static KafkaContainer kafka = new KafkaContainer();

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void configureRoundTrip() throws Exception {
        GlobalKafkaConfiguration g = GlobalKafkaConfiguration.get();
        String kafkaURL = kafka.getBootstrapServers().split("//")[1];
        String zookeeperURL = kafka.getContainerIpAddress() + ":" + kafka.getMappedPort(2181);
        g.setBrokerURL(kafkaURL);
        g.setZookeeperURL(zookeeperURL);
        g.setEnableSSL(false);
        g.save();
        g.load();
        KafkaComputerLauncher launcher = new KafkaComputerLauncher("", "",
                "", "false");
        DumbSlave agent = new DumbSlave("test", "/tmp/", launcher);
        j.jenkins.addNode(agent);
        Computer c = j.jenkins.getComputer("test");
        assertNotNull(c);
        Thread.sleep(10000); // wait to connect master to kafka.
        String[] urls = j.getInstance().getRootUrl().split("/");
        String jenkinsURL = urls[0] + "//" + urls[1] + urls[2] + "/";
        String[] args = new String[]{"-name", "test", "-master", jenkinsURL, "-secret",
                KafkaSecretManager.getConnectionSecret("test"), "-kafkaURL", kafkaURL, "-noauth"};
        AgentRunnable runnable = new AgentRunnable(args);
        Thread t = new Thread(runnable);
        try {
            t.start();
            Thread.sleep(10000); // wait to connect agent to jenkins master.
            FreeStyleProject p = j.createFreeStyleProject();
            p.setAssignedNode(agent);
            j.buildAndAssertSuccess(p);
        } finally {
            t.interrupt();
        }
    }

    private class AgentRunnable implements Runnable {
        private String[] args;

        public AgentRunnable(String[] args) {
            this.args = args;
        }

        @Override
        public void run() {
            try {
                Agent.main(args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
