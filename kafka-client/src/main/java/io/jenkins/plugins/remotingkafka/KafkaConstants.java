package io.jenkins.plugins.remotingkafka;

public class KafkaConstants {
    public static final String CONNECT_SUFFIX = "-connect";
    // Common configs.
    public static final String BOOTSTRAP_SERVERS = "bootstrap.servers";

    // Producer configs. TODO - add more configs.
    public static final String KEY_SERIALIZER = "key.serializer";
    public static final String VALUE_SERIALIZER = "value.serializer";
    public static final String ACKS = "acks";
    public static final String BUFFER_MEMORY = "buffer.memory";
    public static final String COMPRESSION_TYPE = "compression.type";
    public static final String RETRIES = "retries";
    public static final String SSL_KEY_PASSWORD = "ssl.key.password";
    public static final String SSL_KEYSTORE_LOCATION = "ssl.keystore.location";
    public static final String SSL_KEYSTORE_PASSWORD = "ssl.keystore.password";
    public static final String SSL_TRUSTSTORE_LOCATION = "ssl.truststore.location";
    public static final String SSL_TRUSTSTORE_PASSWORD = "ssl.truststore.password";
    public static final String BATCH_SIZE = "batch.size";
    public static final String CLIENT_ID = "client.id";
    public static final String CONNECTIONS_MAX_IDLE = "connections.max.idle.ms";
    public static final String LINGER = "linger.ms";
    public static final String MAX_BLOCK = "max.block.ms";

    // Consumer configs. TODO - add more configs.
    public static final String GROUP_ID = "group.id";
    public static final String ENABLE_AUTO_COMMIT = "enable.auto.commit";
    public static final String KEY_DESERIALIZER = "key.deserializer";
    public static final String VALUE_DESERIALIZER = "value.deserializer";
}
