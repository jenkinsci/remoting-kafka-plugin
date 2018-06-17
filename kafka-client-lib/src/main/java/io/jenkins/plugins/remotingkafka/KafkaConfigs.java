package io.jenkins.plugins.remotingkafka;

public class KafkaConfigs {
    public static final String CONNECT_SUFFIX = "-connect";

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
}
