package io.jenkins.plugins.remotingkafka;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.TaskListener;
import hudson.remoting.*;
import hudson.slaves.ComputerLauncher;
import hudson.slaves.SlaveComputer;
import io.jenkins.plugins.remotingkafka.builder.KafkaClassicCommandTransportBuilder;
import io.jenkins.plugins.remotingkafka.commandtransport.KafkaClassicCommandTransport;
import io.jenkins.plugins.remotingkafka.exception.RemotingKafkaException;
import jenkins.model.JenkinsLocationConfiguration;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.CheckForNull;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.*;
import java.util.concurrent.Future;
import java.util.logging.Logger;

public class KafkaComputerLauncher extends ComputerLauncher {
    private static final Logger LOGGER = Logger.getLogger(KafkaComputerLauncher.class.getName());

    @CheckForNull
    private transient volatile ExecutorService launcherExecutorService;

    @DataBoundConstructor
    public KafkaComputerLauncher() {

    }

    @Override
    public boolean isLaunchSupported() {
        return true;
    }

    @Override
    public synchronized void launch(SlaveComputer computer, final TaskListener listener)
            throws IOException, InterruptedException {
        launcherExecutorService = Executors.newSingleThreadExecutor(
                new NamingThreadFactory(Executors.defaultThreadFactory(),
                        "KafkaComputerLauncher.launch for '" + computer.getName() + "' node"));
        Set<Callable<Boolean>> callables = new HashSet<>();
        callables.add(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                ChannelBuilder cb = new ChannelBuilder(computer.getName(), computer.threadPoolForRemoting)
                        .withHeaderStream(listener.getLogger());
                CommandTransport ct = makeTransport(computer);
                computer.setChannel(cb, ct, new Channel.Listener() {
                    @Override
                    public void onClosed(Channel channel, IOException cause) {
                        super.onClosed(channel, cause);
                    }
                });
                return true;
            }
        });
        try {
            long time = System.currentTimeMillis();
            List<Future<Boolean>> results;
            final ExecutorService srv = launcherExecutorService;
            if (srv == null) {
                throw new IllegalStateException(Messages.KafkaComputerLauncher_NonnullExecutorService());
            }
            results = srv.invokeAll(callables);
            Boolean res;
            try {
                res = results.get(0).get();
            } catch (ExecutionException e) {
                System.out.println(e);
                res = Boolean.FALSE;
            }
            if (!res) {
                listener.getLogger().println(Messages.KafkaComputerLauncher_LaunchFailed());
            } else {
                System.out.println(Messages.KafkaComputerLauncher_LaunchSuccessful());
            }
        } finally {
            ExecutorService srv = launcherExecutorService;
            if (srv != null) {
                srv.shutdownNow();
                launcherExecutorService = null;
            }
        }
    }

    private CommandTransport makeTransport(SlaveComputer computer) throws RemotingKafkaException {
        String nodeName = computer.getName();
        URL jenkinsURL = retrieveJenkinsURL();
        String kafkaURL = getKafkaURL();
        String topic = KafkaConfigs.getConnectionTopic(nodeName, jenkinsURL);
        KafkaUtils.createTopic(topic, GlobalKafkaConfiguration.get().getZookeeperURL(), 2, 1);
        KafkaClassicCommandTransport transport = new KafkaClassicCommandTransportBuilder()
                .withRemoteCapability(new Capability())
                .withProducerKey(KafkaConfigs.getMasterAgentCommandKey(nodeName, jenkinsURL))
                .withConsumerKey(KafkaConfigs.getAgentMasterCommandKey(nodeName, jenkinsURL))
                .withProducerTopic(topic)
                .withConsumerTopic(topic)
                .withProducerPartition(KafkaConfigs.MASTER_AGENT_CMD_PARTITION)
                .withConsumerPartition(KafkaConfigs.AGENT_MASTER_CMD_PARTITION)
                .withProducer(KafkaUtils.createByteProducer(kafkaURL))
                .withConsumer(KafkaUtils.createByteConsumer(kafkaURL,
                        KafkaConfigs.getConsumerGroupID(nodeName, jenkinsURL)))
                .withPollTimeout(0)
                .build();
        return transport;
    }

    public String getKafkaURL() {
        return GlobalKafkaConfiguration.get().getConnectionURL();
    }

    private URL retrieveJenkinsURL() {
        JenkinsLocationConfiguration loc = null;
        try {
            loc = JenkinsLocationConfiguration.get();
        } catch (Exception e) {
            throw new IllegalStateException(Messages.KafkaComputerLauncher_NoJenkinsURL());
        }
        String jenkinsURL = loc.getUrl();
        URL url;
        try {
            if (jenkinsURL == null)
                throw new IllegalStateException(Messages.KafkaComputerLauncher_MalformedJenkinsURL());
            url = new URL(jenkinsURL);
        } catch (MalformedURLException e) {
            throw new IllegalStateException(Messages.KafkaComputerLauncher_MalformedJenkinsURL());
        }
        return url;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<ComputerLauncher> {
        public String getDisplayName() {
            return Messages.KafkaComputerLauncher_DescriptorDisplayName();
        }
    }
}
