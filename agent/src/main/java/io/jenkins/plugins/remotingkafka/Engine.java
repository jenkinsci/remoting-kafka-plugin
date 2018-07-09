package io.jenkins.plugins.remotingkafka;

import hudson.remoting.*;
import io.jenkins.plugins.remotingkafka.builder.KafkaTransportBuilder;
import io.jenkins.plugins.remotingkafka.builder.SecurityPropertiesBuilder;
import io.jenkins.plugins.remotingkafka.commandtransport.KafkaClassicCommandTransport;
import io.jenkins.plugins.remotingkafka.enums.SecurityProtocol;
import io.jenkins.plugins.remotingkafka.exception.RemotingKafkaException;
import io.jenkins.plugins.remotingkafka.security.KafkaPasswordManager;
import org.jenkinsci.remoting.engine.WorkDirManager;
import org.jenkinsci.remoting.protocol.cert.BlindTrustX509ExtendedTrustManager;
import org.jenkinsci.remoting.protocol.cert.DelegatingX509ExtendedTrustManager;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Agent engine that connects to Kafka to communicate with master, similar to
 * hudson.remoting.Engine
 */
public class Engine extends Thread {
    private static final Logger LOGGER = Logger.getLogger(Engine.class.getName());
    private static final ThreadLocal<Engine> CURRENT = new ThreadLocal<>();
    private final EngineListenerSplitter events = new EngineListenerSplitter();
    private final KafkaPasswordManager passwordManager;
    private final Options options;

    /**
     * Thread pool that sets {@link #CURRENT}.
     */
    private final ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactory() {
        private final ThreadFactory defaultFactory = Executors.defaultThreadFactory();

        public Thread newThread(final Runnable r) {
            Thread thread = defaultFactory.newThread(() -> {
                CURRENT.set(Engine.this);
                r.run();
            });
            thread.setDaemon(true);
            thread.setUncaughtExceptionHandler((t, e) -> LOGGER.log(Level.SEVERE, "Uncaught exception in thread " + t, e));
            return thread;
        }
    });

    @CheckForNull
    private Path workDir;
    /**
     * Specifies a destination for the agent log.
     */
    @CheckForNull
    private Path agentLog;
    @CheckForNull
    private JarCache jarCache = null;
    @CheckForNull
    private Path loggingConfigFilePath = null;
    @Nonnull
    private String internalDir = WorkDirManager.DirType.INTERNAL_DIR.getDefaultLocation();
    @Nonnull
    private boolean failIfWorkDirIsMissing = WorkDirManager.DEFAULT_FAIL_IF_WORKDIR_IS_MISSING;
    private URL masterURL;
    private DelegatingX509ExtendedTrustManager agentTrustManager = new DelegatingX509ExtendedTrustManager(new BlindTrustX509ExtendedTrustManager());

    public Engine(EngineListener listener, URL masterURL, Options options, KafkaPasswordManager passwordManager) {
        this.events.add(listener);
        this.masterURL = masterURL;
        this.options = options;
        this.passwordManager = passwordManager;
        if (options.kafkaURL == null || masterURL == null) throw new IllegalArgumentException("No URLs given");
        setUncaughtExceptionHandler((t, e) -> {
            LOGGER.log(Level.SEVERE, "Uncaught exception in Engine thread " + t, e);
            interrupt();
        });
    }

    /**
     * Starts the engine.The procedure initializes the working directory and all the required environment.
     *
     * @throws IOException
     */
    public synchronized void startEngine() throws IOException {
        startEngine(false);
    }

    void startEngine(boolean dryRun) throws IOException {
        LOGGER.log(Level.INFO, "Using Remoting version: {0}", Launcher.VERSION);
        @CheckForNull File jarCacheDirectory = null;

        // Prepare the working directory if required
        if (workDir != null) {
            final WorkDirManager workDirManager = WorkDirManager.getInstance();
            if (jarCache != null) {
                // Somebody has already specificed Jar Cache, hence we do not need it in the workspace.
                workDirManager.disable(WorkDirManager.DirType.JAR_CACHE_DIR);
            }

            if (loggingConfigFilePath != null) {
                workDirManager.setLoggingConfig(loggingConfigFilePath.toFile());
            }

            final Path path = workDirManager.initializeWorkDir(workDir.toFile(), internalDir, failIfWorkDirIsMissing);
            jarCacheDirectory = workDirManager.getLocation(WorkDirManager.DirType.JAR_CACHE_DIR);
            workDirManager.setupLogging(path, agentLog);
        } else if (jarCache == null) {
            LOGGER.log(Level.WARNING, "No Working Directory. Using the legacy JAR Cache location: {0}",
                    JarCache.DEFAULT_NOWS_JAR_CACHE_LOCATION);
            jarCacheDirectory = JarCache.DEFAULT_NOWS_JAR_CACHE_LOCATION;
        }

        if (jarCache == null) {
            if (jarCacheDirectory == null) {
                // Should never happen in the current code
                throw new IOException("Cannot find the JAR Cache location");
            }
            LOGGER.log(Level.FINE, "Using standard File System JAR Cache. Root Directory is {0}", jarCacheDirectory);
            try {
                jarCache = new FileSystemJarCache(jarCacheDirectory, true);
            } catch (IllegalArgumentException ex) {
                throw new IOException("Failed to initialize FileSystem JAR Cache in " + jarCacheDirectory, ex);
            }
        } else {
            LOGGER.log(Level.INFO, "Using custom JAR Cache: {0}", jarCache);
        }

        // Start the engine thread
        if (!dryRun) {
            this.start();
        }
    }

    @Override
    public void run() {
        // Create the engine
        try {
            while (true) {
                ChannelBuilder cb = new ChannelBuilder(options.name, executor)
                        .withJarCacheOrDefault(jarCache);
                CommandTransport transport = makeTransport();
                Channel channel = cb.build(transport);
                if (channel == null) continue;
                events.status("Connected");
                channel.join();
                events.status("Terminated");
            }
        } catch (InterruptedException e) {
            events.error(e);
        } catch (IOException e) {
            events.error(e);
        } catch (RemotingKafkaException e) {
            events.error(e);
        }
    }

    private CommandTransport makeTransport() throws RemotingKafkaException {
        Properties securityProps = null;
        if (!options.noauth) {
            securityProps = new SecurityPropertiesBuilder()
                    .withSSLTruststoreLocation(options.sslTruststoreLocation)
                    .withSSLTruststorePassword(passwordManager.getSslTruststorePassword())
                    .withSSLKeystoreLocation(options.sslKeystoreLocation)
                    .withSSLKeystorePassword(passwordManager.getSslKeystorePassword())
                    .withSSLKeyPassword(passwordManager.getSslKeyPassword())
                    .withSASLJassConfig(options.kafkaUsername, passwordManager.getKafkaPassword())
                    .withSecurityProtocol(SecurityProtocol.SASL_SSL)
                    .withSASLMechanism("PLAIN")
                    .build();
        }
        KafkaClassicCommandTransport transport = new KafkaTransportBuilder()
                .withRemoteCapability(new Capability())
                .withProducerKey(KafkaConfigs.getAgentMasterCommandKey(options.name, masterURL))
                .withConsumerKey(KafkaConfigs.getMasterAgentCommandKey(options.name, masterURL))
                .withProducerTopic(KafkaConfigs.getConnectionTopic(options.name, masterURL))
                .withConsumerTopic(KafkaConfigs.getConnectionTopic(options.name, masterURL))
                .withProducerPartition(KafkaConfigs.AGENT_MASTER_CMD_PARTITION)
                .withConsumerPartition(KafkaConfigs.MASTER_AGENT_CMD_PARTITION)
                .withProducer(KafkaUtils.createByteProducer(options.kafkaURL, securityProps))
                .withConsumer(KafkaUtils.createByteConsumer(options.kafkaURL,
                        KafkaConfigs.getConsumerGroupID(options.name, masterURL), securityProps))
                .withPollTimeout(0)
                .build();
        return transport;
    }

    public void setInternalDir(@Nonnull String internalDir) {
        this.internalDir = internalDir;
    }

    public void setFailIfWorkDirIsMissing(boolean failIfWorkDirIsMissing) {
        this.failIfWorkDirIsMissing = failIfWorkDirIsMissing;
    }
}
