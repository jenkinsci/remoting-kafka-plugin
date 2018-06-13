package io.jenkins.plugins.remotingkafka;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.remoting.*;
import hudson.slaves.ComputerLauncher;
import hudson.slaves.SlaveComputer;
import io.jenkins.plugins.remotingkafka.commandtransport.KafkaClassicCommandTransport;
import jenkins.model.JenkinsLocationConfiguration;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.Producer;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.CheckForNull;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
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
    public synchronized void launch(SlaveComputer computer, final TaskListener listener) throws IOException, InterruptedException {
        launcherExecutorService = Executors.newSingleThreadExecutor(
                new NamingThreadFactory(Executors.defaultThreadFactory(), "KafkaComputerLauncher.launch for '" + computer.getName() + "' node"));
        Set<Callable<Boolean>> callables = new HashSet<>();
        callables.add(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                Node node = computer.getNode();
                if (node == null) return false;
                ChannelBuilder cb = new ChannelBuilder(node.getNodeName(), computer.threadPoolForRemoting)
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
                throw new IllegalStateException("Launcher Executor Service should be always non-null here, because the task allocates and closes service on its own");
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
                listener.getLogger().println("Launch failed");
            } else {
                System.out.println("Launch successfully");
            }
        } catch (InterruptedException e) {
            System.out.println("Interrupted Exception");
        } finally {
            ExecutorService srv = launcherExecutorService;
            if (srv != null) {
                srv.shutdownNow();
                launcherExecutorService = null;
            }
        }
    }

    private CommandTransport makeTransport(SlaveComputer computer) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        JenkinsLocationConfiguration loc = JenkinsLocationConfiguration.get();
        String nodeName = computer.getName();
        String jenkinsURL = loc.getUrl();
        URL url;
        try {
            if (jenkinsURL == null) throw new IllegalStateException("Malformed Jenkins URL exception");
            url = new URL(jenkinsURL);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Malformed Jenkins URL exception");
        }
        Capability cap = new Capability();
        String producerKey = nodeName, consumerKey = nodeName;
        String producerTopic = url.getHost() + "-" + url.getPort() + "-" + nodeName
                + KafkaConfigs.CONNECT_SUFFIX;
        List<String> consumerTopics = Arrays.asList(nodeName + "-" + url.getHost() + "-" + url.getPort()
                + KafkaConfigs.CONNECT_SUFFIX);

        Properties producerProps = GlobalKafkaConfiguration.get().getProducerProps();
        if (producerProps.getProperty(KafkaConfigs.BOOTSTRAP_SERVERS) == null) {
            throw new IllegalStateException("Please provide Kafka producer connection URL in global setting");
        }
        producerProps.put(KafkaConfigs.KEY_SERIALIZER, "org.apache.kafka.common.serialization.StringSerializer");
        producerProps.put(KafkaConfigs.VALUE_SERIALIZER, "org.apache.kafka.common.serialization.ByteArraySerializer");
        Producer<String, byte[]> producer = KafkaProducerClient.getInstance().getByteProducer(producerProps);
        Properties consumerProps = GlobalKafkaConfiguration.get().getConsumerProps();
        if (consumerProps.getProperty(KafkaConfigs.BOOTSTRAP_SERVERS) == null) {
            throw new IllegalStateException("Please provide Kafka consumer connection URL in global setting");
        }
        consumerProps.put(KafkaConfigs.GROUP_ID, "master-" + nodeName);
        consumerProps.put(KafkaConfigs.KEY_DESERIALIZER, "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put(KafkaConfigs.VALUE_DESERIALIZER, "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        Thread.currentThread().setContextClassLoader(null);
        KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(consumerProps);
        Thread.currentThread().setContextClassLoader(cl);
        return new KafkaClassicCommandTransport(cap, producerTopic, producerKey, consumerTopics, consumerKey, 0, producer, consumer);
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<ComputerLauncher> {
        public String getDisplayName() {
            return "Launch agents with Kafka";
        }
    }
}
