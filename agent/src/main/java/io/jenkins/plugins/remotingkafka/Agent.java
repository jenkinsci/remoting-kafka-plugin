package io.jenkins.plugins.remotingkafka;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Agent {
    private static final Logger LOGGER = Logger.getLogger(Agent.class.getName());

    private final Options options;

    public Agent(Options options) {
        this.options = options;
    }

    public static void main(String... args) throws InterruptedException, IOException {
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
                LOGGER.severe("Failed to lookup the canonical hostname of this slave, please check system settings.");
                LOGGER.severe("If not possible to resolve please specify a node name using the '-name' option");
                System.exit(-1);
            }
        }

        // Kafka setup.
        KafkaProducerClient producer = KafkaProducerClient.getInstance();
        KafkaConsumerClient consumer = KafkaConsumerClient.getInstance();
        URL url = new URL(options.master);
        String masterAgentConnectionTopic = url.getHost() + "-" + url.getPort() + "-" + options.name
                + KafkaConstants.CONNECT_SUFFIX;
        String agentMasterConnectionTopic = options.name + "-" + url.getHost() + "-" + url.getPort()
                + KafkaConstants.CONNECT_SUFFIX;
        consumer.subscribe(options.kafkaURL, Arrays.asList(masterAgentConnectionTopic), 0);
        producer.send(options.kafkaURL, agentMasterConnectionTopic, null, "acked from " + options.name);
    }
}
