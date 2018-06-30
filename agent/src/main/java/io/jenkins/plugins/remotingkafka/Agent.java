package io.jenkins.plugins.remotingkafka;

import hudson.remoting.EngineListener;
import io.jenkins.plugins.remotingkafka.builder.KafkaTransportBuilder;
import io.jenkins.plugins.remotingkafka.builder.SecurityPropertiesBuilder;
import io.jenkins.plugins.remotingkafka.enums.SecurityProtocol;
import io.jenkins.plugins.remotingkafka.exception.RemotingKafkaConfigurationException;
import org.jenkinsci.remoting.engine.WorkDirManager;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;

public class Agent {
    private static final Logger LOGGER = Logger.getLogger(Agent.class.getName());

    private final Options options;

    public Agent(Options options) {
        this.options = options;
    }

    public static void main(String... args) throws InterruptedException, IOException, RemotingKafkaConfigurationException {
        Options options = new Options();
        Agent agent = new Agent(options);

        CmdLineParser p = new CmdLineParser(options);
        try {
            p.parseArgument(args);
        } catch (CmdLineException e) {
            LOGGER.log(Level.SEVERE, "CmdLineException occurred during parseArgument", e);
            p.printUsage(System.out);
            System.exit(-1);
        }

        if (options.help) {
            p.printUsage(System.out);
            System.exit(0);
        }

        if (options.name == null) {
            try {
                agent.options.name = InetAddress.getLocalHost().getCanonicalHostName();
            } catch (IOException e) {
                LOGGER.severe("Failed to lookup the canonical hostname of this agent, please check system settings.");
                LOGGER.severe("If not possible to resolve please specify a node name using the '-name' option");
                System.exit(-1);
            }
        }

        if (options.secret == null) {
            LOGGER.info("Please provide a secret");
            System.exit(-1);
        }
        URL masterURL = new URL(options.master);
        String topic = KafkaConfigs.getConnectionTopic(options.name, masterURL);
        Properties securityProps = new SecurityPropertiesBuilder()
                .withSSLTruststoreLocation("../../certs/docker.kafka.server.truststore.jks")
                .withSSLTruststorePassword("kafkadocker")
                .withSSLKeystoreLocation("../../certs/docker.kafka.server.keystore.jks")
                .withSSLKeystorePassword("kafkadocker")
                .withSSLKeyPassword("kafkadocker")
                .withSASLJassConfig("user", "password")
                .withSecurityProtocol(SecurityProtocol.SASL_SSL)
                .withSASLMechanism("PLAIN")
                .build();
        KafkaTransportBuilder listenerSettings = new KafkaTransportBuilder()
                .withProducer(KafkaUtils.createByteProducer(options.kafkaURL, securityProps))
                .withConsumer(KafkaUtils.createByteConsumer(options.kafkaURL,
                        KafkaConfigs.getConsumerGroupID(options.name, masterURL), securityProps))
                .withProducerTopic(topic)
                .withConsumerTopic(topic)
                .withProducerKey(KafkaConfigs.getAgentMasterSecretKey(options.name, masterURL))
                .withConsumerKey(KafkaConfigs.getMasterAgentSecretKey(options.name, masterURL))
                .withProducerPartition(KafkaConfigs.AGENT_MASTER_SECRET_PARTITION)
                .withConsumerPartition(KafkaConfigs.MASTER_AGENT_SECRET_PARTITION);
        KafkaClientListener secretListener = new KafkaClientListener("hello", options.secret, listenerSettings);
        new Thread(secretListener).start();
        Engine engine = new Engine(new CuiListener(), masterURL, options.name, options.kafkaURL, options.secret);
        engine.setInternalDir(WorkDirManager.DirType.INTERNAL_DIR.getDefaultLocation());
        engine.setFailIfWorkDirIsMissing(WorkDirManager.DEFAULT_FAIL_IF_WORKDIR_IS_MISSING);
        engine.startEngine();
        try {
            engine.join();
            LOGGER.fine("Engine has died");
        } finally {
            engine.interrupt();
        }
    }

    private static final class CuiListener implements EngineListener {
        private CuiListener() {
            LOGGER.info("Jenkins agent is running in headless mode.");
        }

        public void status(String msg, Throwable t) {
            LOGGER.log(INFO, msg, t);
        }

        public void status(String msg) {
            status(msg, null);
        }

        public void error(Throwable t) {
            LOGGER.log(Level.SEVERE, t.getMessage(), t);
        }

        public void onDisconnect() {
        }

        public void onReconnect() {
        }
    }
}
