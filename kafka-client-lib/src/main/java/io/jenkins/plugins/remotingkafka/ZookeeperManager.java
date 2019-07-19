package io.jenkins.plugins.remotingkafka;

import io.jenkins.plugins.remotingkafka.exception.RemotingKafkaConfigurationException;
import kafka.admin.AdminUtils;
import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ZookeeperManager {
    private static final Logger LOGGER = Logger.getLogger(ZookeeperManager.class.getName());
    private static final int SESSION_TIMEOUT = 15 * 1000; // 15 secs.
    private static final int CONNECTION_TIMEOUT = 10 * 1000; // 10 secs.

    private static volatile ZookeeperManager manager;

    private ZkClient zkClient;

    private ZkUtils zkUtils;

    private Properties topicConfiguration;

    private String zookeeperHost;

    private ZookeeperManager() {
    }

    public static ZookeeperManager getInstance() {
        if (manager == null) {
            synchronized (ZookeeperManager.class) {
                if (manager == null) {
                    manager = new ZookeeperManager();
                }
            }
        }
        return manager;
    }

    public void init(String zookeeperHost) throws RemotingKafkaConfigurationException {
        if (this.zookeeperHost == null) {
            this.zookeeperHost = zookeeperHost;
            try {
                zkClient = new ZkClient(zookeeperHost, SESSION_TIMEOUT, CONNECTION_TIMEOUT, ZKStringSerializer$.MODULE$);
                zkUtils = new ZkUtils(zkClient, new ZkConnection(zookeeperHost), false);
                topicConfiguration = new Properties();
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, ex.toString(), ex);
                throw new RemotingKafkaConfigurationException(ex.toString(), ex);
            }
        }
    }

    public void createTopic(String topic, int noOfPartitions, int noOfReplication) {
        if (!AdminUtils.topicExists(zkUtils, topic)) {
            AdminUtils.createTopic(zkUtils, topic, noOfPartitions, noOfReplication, topicConfiguration, null);
        }
    }

    public void close() {
        if (zkClient != null) {
            zkClient.close();
        }
    }
}
