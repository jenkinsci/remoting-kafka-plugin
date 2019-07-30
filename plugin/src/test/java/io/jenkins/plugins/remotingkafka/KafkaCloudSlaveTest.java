package io.jenkins.plugins.remotingkafka;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class KafkaCloudSlaveTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

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
