package io.jenkins.plugins.remotingkafka;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.TaskListener;
import hudson.slaves.ComputerLauncher;
import hudson.slaves.SlaveComputer;
import jenkins.model.JenkinsLocationConfiguration;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.logging.Logger;

public class KafkaComputerLauncher extends ComputerLauncher {
    private static final Logger LOGGER = Logger.getLogger(KafkaComputerLauncher.class.getName());

    @DataBoundConstructor
    public KafkaComputerLauncher() {

    }

    @Override
    public boolean isLaunchSupported() {
        return true;
    }

    @Override
    public synchronized void launch(final SlaveComputer computer, final TaskListener listener)
            throws InterruptedException, IOException {
        KafkaProducerClient producer = KafkaProducerClient.getInstance();
        KafkaConsumerClient consumer = KafkaConsumerClient.getInstance();
        JenkinsLocationConfiguration loc = JenkinsLocationConfiguration.get();
        String jenkinsURL = (loc.getUrl() == null) ? "http://localhost:8080" : loc.getUrl();
        URL url = new URL(jenkinsURL);
        String masterAgentConnectionTopic = url.getHost() + "-" + url.getPort() + "-" + computer.getName()
                + KafkaConstants.CONNECT_SUFFIX;
        String agentMasterConnectionTopic = computer.getName() + "-" + url.getHost() + "-" + url.getPort()
                + KafkaConstants.CONNECT_SUFFIX;
        String kafkaURL = GlobalKafkaConfiguration.get().getConnectionURL();
        producer.send(kafkaURL, masterAgentConnectionTopic, null, agentMasterConnectionTopic);
        consumer.subscribe(kafkaURL, Arrays.asList(agentMasterConnectionTopic), 0);
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<ComputerLauncher> {
        public String getDisplayName() {
            return "Launch agents with Kafka";
        }
    }
}
