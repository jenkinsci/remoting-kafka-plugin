package io.jenkins.plugins.remotingkafka;

import hudson.slaves.AbstractCloudComputer;

import java.util.logging.Logger;

public class KafkaCloudComputer extends AbstractCloudComputer<KafkaCloudSlave> {

    public KafkaCloudComputer(KafkaCloudSlave agent) {
        super(agent);
    }

}
