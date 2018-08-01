package io.jenkins.plugins.remotingkafka;

import com.salesforce.kafka.test.junit4.SharedKafkaTestResource;
import hudson.model.Computer;
import hudson.model.FreeStyleProject;
import hudson.slaves.DumbSlave;
import io.jenkins.plugins.remotingkafka.util.RemotingKafkaAgentContainer;
import org.jenkinsci.test.acceptance.docker.DockerRule;
import org.jenkinsci.utils.process.ProcessInputStream;
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

    @Rule
    public DockerRule<RemotingKafkaAgentContainer> agentContainer
            = new DockerRule<>(RemotingKafkaAgentContainer.class);

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
        String[] args = new String[]{"java", "-jar", "/remoting-kafka-agent.jar", "-name", "test", "-master", jenkinsURL, "-secret",
                KafkaSecretManager.getConnectionSecret("test"), "-kafkaURL", sharedKafkaTestResource.getKafkaConnectString(), "-noauth",
                "1>~/log.txt", "2>&1"
        };

        ProcessInputStream i = agentContainer.get().ssh().add(args).popen();
        try {
            Thread.sleep(10000); // wait to connect agent to jenkins master.
            FreeStyleProject p = j.createFreeStyleProject();
            p.setAssignedNode(slave);
          //  j.buildAndAssertSuccess(p);
        } finally {
            System.out.println(i.asText());
            i.close();
        }
    }

}
