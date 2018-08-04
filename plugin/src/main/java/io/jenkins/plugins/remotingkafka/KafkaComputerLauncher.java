package io.jenkins.plugins.remotingkafka;

import hudson.AbortException;
import hudson.Extension;
import hudson.model.Computer;
import hudson.model.Descriptor;
import hudson.model.TaskListener;
import hudson.remoting.*;
import hudson.slaves.ComputerLauncher;
import hudson.slaves.SlaveComputer;
import hudson.util.FormValidation;
import io.jenkins.plugins.remotingkafka.builder.KafkaTransportBuilder;
import io.jenkins.plugins.remotingkafka.builder.SecurityPropertiesBuilder;
import io.jenkins.plugins.remotingkafka.commandtransport.KafkaClassicCommandTransport;
import io.jenkins.plugins.remotingkafka.enums.SecurityProtocol;
import io.jenkins.plugins.remotingkafka.exception.RemotingKafkaConfigurationException;
import io.jenkins.plugins.remotingkafka.exception.RemotingKafkaException;
import jenkins.model.JenkinsLocationConfiguration;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.*;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KafkaComputerLauncher extends ComputerLauncher {
    private static final Logger LOGGER = Logger.getLogger(KafkaComputerLauncher.class.getName());
    private static final long DEFAULT_TIMEOUT = 60000;

    @CheckForNull
    private transient volatile ExecutorService launcherExecutorService;

    private String kafkaUsername;

    private String sslTruststoreLocation;

    private String sslKeystoreLocation;

    private boolean enableSSL;

    @DataBoundConstructor
    public KafkaComputerLauncher(String kafkaUsername, String sslTruststoreLocation, String sslKeystoreLocation,
                                 String enableSSL) {
        this.kafkaUsername = kafkaUsername;
        this.sslTruststoreLocation = sslTruststoreLocation;
        this.sslKeystoreLocation = sslKeystoreLocation;
        this.enableSSL = Boolean.valueOf(enableSSL);
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
                Boolean rval = Boolean.FALSE;
                String topic = KafkaConfigs.getConnectionTopic(computer.getName(), retrieveJenkinsURL());
                KafkaUtils.createTopic(topic, GlobalKafkaConfiguration.get().getZookeeperURL(),
                        4, 1);
                if (!isValidAgent(computer.getName(), listener)) {
                    return Boolean.FALSE;
                }
                try {
                    ChannelBuilder cb = new ChannelBuilder(computer.getName(), computer.threadPoolForRemoting)
                            .withHeaderStream(listener.getLogger());
                    CommandTransport ct = makeTransport(computer);
                    computer.setChannel(cb, ct, new Channel.Listener() {
                        @Override
                        public void onClosed(Channel channel, IOException cause) {
                            super.onClosed(channel, cause);
                        }
                    });
                    rval = Boolean.TRUE;
                } catch (RuntimeException e) {
                    e.printStackTrace(listener.error(Messages.KafkaComputerLauncher_UnexpectedError()));
                } catch (Error e) {
                    e.printStackTrace(listener.error(Messages.KafkaComputerLauncher_UnexpectedError()));
                } catch (AbortException e) {
                    listener.getLogger().println(e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace(listener.getLogger());
                } catch (Exception e) {
                    listener.getLogger().println(e.getMessage());
                } finally {
                    return rval;
                }
            }
        });
        try {
            List<Future<Boolean>> results;
            final ExecutorService srv = launcherExecutorService;
            if (srv == null) {
                throw new IllegalStateException(Messages.KafkaComputerLauncher_NonnullExecutorService());
            }
            results = srv.invokeAll(callables, DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
            Boolean res;
            try {
                res = results.get(0).get();
            } catch (CancellationException | ExecutionException e) {
                LOGGER.log(Level.SEVERE, "Execution exception when launch: ", e);
                res = Boolean.FALSE;
            }
            if (!res) {
                listener.getLogger().println(Messages.KafkaComputerLauncher_LaunchFailed());
            } else {
                listener.getLogger().println(Messages.KafkaComputerLauncher_LaunchSuccessful());
            }
        } catch (InterruptedException e) {
            listener.getLogger().println(Messages.KafkaComputerLauncher_LaunchFailed());
        } finally {
            ExecutorService srv = launcherExecutorService;
            if (srv != null) {
                srv.shutdownNow();
                launcherExecutorService = null;
            }
        }
    }

    @Override
    public void afterDisconnect(SlaveComputer slaveComputer, final TaskListener listener) {
        ExecutorService srv = launcherExecutorService;
        if (srv != null) {
            // If the service is still running, shut it down and interrupt the operations if any
            srv.shutdown();
        }
    }

    private CommandTransport makeTransport(SlaveComputer computer) throws RemotingKafkaException {
        String nodeName = computer.getName();
        URL jenkinsURL = retrieveJenkinsURL();
        String kafkaURL = getKafkaURL();
        String topic = KafkaConfigs.getConnectionTopic(nodeName, jenkinsURL);
        GlobalKafkaConfiguration kafkaConfig = GlobalKafkaConfiguration.get();
        Properties securityProps = null;
        if (kafkaConfig.getEnableSSL()) {
            securityProps = new SecurityPropertiesBuilder()
                    .withSSLTruststoreLocation(kafkaConfig.getSSLTruststoreLocation())
                    .withSSLTruststorePassword(kafkaConfig.getSSLTruststorePassword())
                    .withSSLKeystoreLocation(kafkaConfig.getSSLKeystoreLocation())
                    .withSSLKeystorePassword(kafkaConfig.getSSLKeystorePassword())
                    .withSSLKeyPassword(kafkaConfig.getSSLKeyPassword())
                    .withSASLJassConfig(kafkaConfig.getKafkaUsername(), kafkaConfig.getKafkaPassword())
                    .withSecurityProtocol(SecurityProtocol.SASL_SSL)
                    .withSASLMechanism("PLAIN")
                    .build();
        }
        KafkaClassicCommandTransport transport = new KafkaTransportBuilder()
                .withRemoteCapability(new Capability())
                .withProducerKey(KafkaConfigs.getMasterAgentCommandKey(nodeName, jenkinsURL))
                .withConsumerKey(KafkaConfigs.getAgentMasterCommandKey(nodeName, jenkinsURL))
                .withProducerTopic(topic)
                .withConsumerTopic(topic)
                .withProducerPartition(KafkaConfigs.MASTER_AGENT_CMD_PARTITION)
                .withConsumerPartition(KafkaConfigs.AGENT_MASTER_CMD_PARTITION)
                .withProducer(KafkaUtils.createByteProducer(kafkaURL, securityProps))
                .withConsumer(KafkaUtils.createByteConsumer(kafkaURL,
                        KafkaConfigs.getConsumerGroupID(nodeName, jenkinsURL), securityProps))
                .withPollTimeout(0)
                .build();
        return transport;
    }

    public String getLaunchSecret(@Nonnull Computer computer) {
        return KafkaSecretManager.getConnectionSecret(computer.getName());
    }

    public String getKafkaURL() {
        return GlobalKafkaConfiguration.get().getBrokerURL();
    }

    public String getKafkaUsername() {
        return kafkaUsername;
    }

    public void setKafkaUsername(String kafkaUsername) {
        this.kafkaUsername = kafkaUsername;
    }

    public String getSslTruststoreLocation() {
        return sslTruststoreLocation;
    }

    public void setSslTruststoreLocation(String sslTruststoreLocation) {
        this.sslTruststoreLocation = sslTruststoreLocation;
    }

    public String getSslKeystoreLocation() {
        return sslKeystoreLocation;
    }

    public void setSslKeystoreLocation(String sslKeystoreLocation) {
        this.sslKeystoreLocation = sslKeystoreLocation;
    }

    public boolean getEnableSSL() {
        return enableSSL;
    }

    public void setEnableSSL(boolean enableSSL) {
        this.enableSSL = enableSSL;
    }

    private URL retrieveJenkinsURL() throws RemotingKafkaConfigurationException {
        JenkinsLocationConfiguration loc = null;
        try {
            loc = JenkinsLocationConfiguration.get();
        } catch (Exception e) {
            throw new RemotingKafkaConfigurationException(Messages.KafkaComputerLauncher_NoJenkinsURL());
        }
        String jenkinsURL = loc.getUrl();
        URL url;
        try {
            if (jenkinsURL == null)
                throw new RemotingKafkaConfigurationException(Messages.KafkaComputerLauncher_MalformedJenkinsURL());
            url = new URL(jenkinsURL);
        } catch (MalformedURLException e) {
            throw new RemotingKafkaConfigurationException(Messages.KafkaComputerLauncher_MalformedJenkinsURL());
        }
        return url;
    }

    /**
     * Wait for secret confirmation from agent.
     *
     * @param agentName
     * @return
     * @throws RemotingKafkaConfigurationException
     */
    private boolean isValidAgent(@Nonnull String agentName, TaskListener listener)
            throws RemotingKafkaException, InterruptedException {
        String kafkaURL = getKafkaURL();
        URL jenkinsURL = retrieveJenkinsURL();
        String topic = KafkaConfigs.getConnectionTopic(agentName, jenkinsURL);
        GlobalKafkaConfiguration kafkaConfig = GlobalKafkaConfiguration.get();
        Properties securityProps = null;
        if (kafkaConfig.getEnableSSL()) {
            securityProps = new SecurityPropertiesBuilder()
                    .withSSLTruststoreLocation(kafkaConfig.getSSLTruststoreLocation())
                    .withSSLTruststorePassword(kafkaConfig.getSSLTruststorePassword())
                    .withSSLKeystoreLocation(kafkaConfig.getSSLKeystoreLocation())
                    .withSSLKeystorePassword(kafkaConfig.getSSLKeystorePassword())
                    .withSSLKeyPassword(kafkaConfig.getSSLKeyPassword())
                    .withSASLJassConfig(kafkaConfig.getKafkaUsername(), kafkaConfig.getKafkaPassword())
                    .withSecurityProtocol(SecurityProtocol.SASL_SSL)
                    .withSASLMechanism("PLAIN")
                    .build();
        }
        KafkaTransportBuilder settings = new KafkaTransportBuilder()
                .withProducer(KafkaUtils.createByteProducer(kafkaURL, securityProps))
                .withConsumer(KafkaUtils.createByteConsumer(kafkaURL,
                        KafkaConfigs.getConsumerGroupID(agentName, jenkinsURL), securityProps))
                .withProducerKey(KafkaConfigs.getMasterAgentSecretKey(agentName, jenkinsURL))
                .withConsumerKey(KafkaConfigs.getAgentMasterSecretKey(agentName, jenkinsURL))
                .withProducerTopic(topic)
                .withConsumerTopic(topic)
                .withProducerPartition(KafkaConfigs.MASTER_AGENT_SECRET_PARTITION)
                .withConsumerPartition(KafkaConfigs.AGENT_MASTER_SECRET_PARTITION);
        KafkaSecretManager secretManager = new KafkaSecretManager(agentName, settings, DEFAULT_TIMEOUT, listener);
        return secretManager.waitForValidAgent();
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<ComputerLauncher> {
        public String getDisplayName() {
            return Messages.KafkaComputerLauncher_DescriptorDisplayName();
        }

        public FormValidation doCheckKafkaUsername(@QueryParameter("kafkaUsername") String kafkaUsername) {
            if (StringUtils.isBlank(kafkaUsername)) {
                return FormValidation.warning(Messages.GlobalKafkaConfiguration_KafkaSecurityWarning());
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckSslTruststoreLocation(@QueryParameter("sslTruststoreLocation") String sslTruststoreLocation) {
            if (StringUtils.isBlank(sslTruststoreLocation)) {
                return FormValidation.warning(Messages.GlobalKafkaConfiguration_KafkaSecurityWarning());
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckSslKeystoreLocation(@QueryParameter("sslKeystoreLocation") String sslKeystoreLocation) {
            if (StringUtils.isBlank(sslKeystoreLocation)) {
                return FormValidation.warning(Messages.GlobalKafkaConfiguration_KafkaSecurityWarning());
            }
            return FormValidation.ok();
        }
    }
}