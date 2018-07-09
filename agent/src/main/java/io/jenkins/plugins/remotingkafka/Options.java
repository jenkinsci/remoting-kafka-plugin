package io.jenkins.plugins.remotingkafka;

import org.kohsuke.args4j.Option;

public class Options {

    @Option(name = "-name", usage = "Name of the agent")
    public String name;

    @Option(name = "-master", usage = "The complete target Jenkins URL like 'http://server:8080/jenkins/'.")
    public String master;

    @Option(name = "-help", aliases = "--help", usage = "Show the help screen")
    public boolean help;

    @Option(name = "-kafkaURL", usage = "Kafka host and port address identifier")
    public String kafkaURL;

    @Option(name = "-secret", usage = "Secret to send to master to establish a secure connection")
    public String secret;

    @Option(name = "-kafkaUsername", usage = "Username to login to kafka broker")
    public String kafkaUsername;

    @Option(name = "-sslTruststoreLocation", usage = "Truststore location to do SSL handshake to Kafka broker")
    public String sslTruststoreLocation;

    @Option(name = "-sslKeystoreLocation", usage = "Username to login to kafka broker")
    public String sslKeystoreLocation;

    @Option(name = "-noauth", usage = "Enable to connect agent to master without Kafka authorization")
    public boolean noauth;
}
