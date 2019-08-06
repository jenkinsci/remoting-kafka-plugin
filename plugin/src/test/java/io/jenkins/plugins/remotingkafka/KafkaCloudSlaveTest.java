package io.jenkins.plugins.remotingkafka;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import hudson.model.Descriptor;
import org.apache.commons.lang.StringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;

public class KafkaCloudSlaveTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void testSlaveInitWithCloudOfNullArguments() throws Descriptor.FormException, IOException {
        final KafkaKubernetesCloud cloud = new KafkaKubernetesCloud("kafka-kubernetes");
        cloud.setDescription(null);
        cloud.setWorkingDir(null);
        cloud.setNodeUsageMode(null);
        cloud.setIdleMinutes(null);
        cloud.setLabel(null);
        cloud.setNodeProperties(null);
        new KafkaCloudSlave(cloud);
    }

    @Test
    public void testGetSlaveNameBlankBaseName() {
        String baseName = null;
        String name = KafkaCloudSlave.getSlaveName(baseName);
        assertTrue(StringUtils.isNotBlank(name));
        baseName = "";
        name = KafkaCloudSlave.getSlaveName(baseName);
        assertTrue(StringUtils.isNotBlank(name));
    }

    @Test
    public void testGetSlaveNameIsRandom() {
        final String baseName = "base-name";
        final String name1 = KafkaCloudSlave.getSlaveName(baseName);
        final String name2 = KafkaCloudSlave.getSlaveName(baseName);
        assertThat(name1, is(not(name2)));
    }

    @Test
    public void testGetSlaveNameContainBaseName() {
        final String baseName = "base-name";
        final String name = KafkaCloudSlave.getSlaveName(baseName);
        assertThat(name, containsString(baseName));
    }

}
