package io.jenkins.plugins.remotingkafka.util;

import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerFixture;
import org.jenkinsci.test.acceptance.docker.DockerImage;
import org.jenkinsci.test.acceptance.docker.fixtures.SshdContainer;
import org.jenkinsci.utils.process.CommandBuilder;

import java.io.File;
import java.io.IOException;

/**
 * @author Oleg Nenashev
 * @since TODO
 */
@DockerFixture(id = "remoting-kafka-agent", ports = 22)
public class RemotingKafkaAgentContainer extends SshdContainer {

}
