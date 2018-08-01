package io.jenkins.plugins.remotingkafka;

import com.salesforce.kafka.test.junit4.SharedKafkaTestResource;
import hudson.model.Computer;
import hudson.model.FreeStyleProject;
import hudson.slaves.DumbSlave;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.junit.Assert.assertNotNull;

public class KafkaComputerLauncherTest {
    @ClassRule
    public static final SharedKafkaTestResource sharedKafkaTestResource = new SharedKafkaTestResource();

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void configureRoundTrip() throws Exception {
        GlobalKafkaConfiguration g = GlobalKafkaConfiguration.get();
        g.setBrokerURL(sharedKafkaTestResource.getKafkaConnectString());
        g.setZookeeperURL(sharedKafkaTestResource.getZookeeperConnectString());
        g.setEnableSSL(false);
        g.save();
        g.load();
        KafkaComputerLauncher launcher = new KafkaComputerLauncher("", "",
                "", "false");
        DumbSlave slave = new DumbSlave("test", "/tmp/", launcher);
        j.jenkins.addNode(slave);
        Computer c = j.jenkins.getComputer("test");
        assertNotNull(c);
        String[] urls = j.getInstance().getRootUrl().split("/");
        String jenkinsURL = urls[0] + "//" + urls[1] + urls[2] + "/";
        String[] args = new String[]{"-name", "test", "-master", jenkinsURL, "-secret",
                KafkaSecretManager.getConnectionSecret("test"), "-kafkaURL", sharedKafkaTestResource.getKafkaConnectString(), "-noauth"};
        AgentRunnable runnable = new AgentRunnable(args);
        Thread t = new Thread(runnable);
        t.start();
        Thread.sleep(10000); // wait to connect agent to jenkins master.
        FreeStyleProject p = j.createFreeStyleProject();
        p.setAssignedNode(slave);
        j.buildAndAssertSuccess(p);
        t.interrupt();
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
