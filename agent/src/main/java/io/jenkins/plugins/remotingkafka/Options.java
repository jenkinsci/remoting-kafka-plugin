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
}
