package io.jenkins.plugins.remotingkafka;

import hudson.slaves.AbstractCloudComputer;

import java.util.logging.Logger;

public class KafkaCloudComputer extends AbstractCloudComputer<KafkaCloudSlave> {
    private static final Logger LOGGER = Logger.getLogger(KafkaCloudComputer.class.getName());

    public KafkaCloudComputer(KafkaCloudSlave slave) {
        super(slave);
    }

}
