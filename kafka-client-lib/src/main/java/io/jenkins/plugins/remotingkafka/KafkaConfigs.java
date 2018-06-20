package io.jenkins.plugins.remotingkafka;

import java.net.URL;

public class KafkaConfigs {
    private static final String DELIMITER = ".";
    private static final String TOPIC_SUFFIX = "-topic";
    private static final String CONSUMER_GROUP_SUFFIX = "-id";
    private static final String COMMAND_SUFFIX = "-command";

    // Common configs.
    public static final String BOOTSTRAP_SERVERS = "bootstrap.servers";

    // Producer configs. TODO - add more configs.
    public static final String KEY_SERIALIZER = "key.serializer";
    public static final String VALUE_SERIALIZER = "value.serializer";
    public static final String ACKS = "acks";

    // Consumer configs. TODO - add more configs.
    public static final String GROUP_ID = "group.id";
    public static final String ENABLE_AUTO_COMMIT = "enable.auto.commit";
    public static final String KEY_DESERIALIZER = "key.deserializer";
    public static final String VALUE_DESERIALIZER = "value.deserializer";
    public static final String AUTO_OFFSET_RESET = "auto.offset.reset";

    // Partition configs.
    public static final int MASTER_AGENT_CMD_PARTITION = 0;
    public static final int AGENT_MASTER_CMD_PARTITION = 1;

    // Kafka topic configs.
    public static String getConnectionTopic(String agentName, URL masterURL) {
        return masterURL.getHost() + DELIMITER + masterURL.getPort() + DELIMITER + agentName + TOPIC_SUFFIX;
    }

    // Consumer group ID configs.
    public static String getConsumerGroupID(String agentName, URL masterURL) {
        return masterURL.getHost() + DELIMITER + masterURL.getPort() + DELIMITER + agentName + CONSUMER_GROUP_SUFFIX;
    }

    // Producer key configs.
    public static String getMasterAgentCommandKey(String agentName, URL masterURL) {
        return masterURL.getHost() + DELIMITER + masterURL.getPort() + DELIMITER + agentName + COMMAND_SUFFIX;
    }

    public static String getAgentMasterCommandKey(String agentName, URL masterURL) {
        return agentName + DELIMITER + masterURL.getHost() + DELIMITER + masterURL.getPort() + COMMAND_SUFFIX;
    }
}
