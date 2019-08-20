Changelog
===

# 2.0.1

Release date: August 19, 2019

* [JENKINS-58690](https://issues.jenkins-ci.org/browse/JENKINS-58690) - Retention strategy for Cloud nodes
* [JENKINS-57898](https://issues.jenkins-ci.org/browse/JENKINS-57898), [JENKINS-58786](https://issues.jenkins-ci.org/browse/JENKINS-58786) - Increate test coverage to 70%
* Update dependencies and Kafka versions

# 2.0.0

Release date: July 30, 2019

* [JENKINS-57668](https://issues.jenkins-ci.org/browse/JENKINS-57668) - Implement Cloud API to provision Kafka agents
* [JENKINS-58288](https://issues.jenkins-ci.org/browse/JENKINS-58288) - Helm Chart to bootstrap the system in Kubernetes
* Add more unit tests

# 2.0.1-beta

Release date: July 24, 2019

* [JENKINS-58688](https://issues.jenkins-ci.org/browse/JENKINS-58688) - Separate Kubernetes configuration fields for Cloud

# 2.0.0-beta

Release date: July 23, 2019

* [JENKINS-57668](https://issues.jenkins-ci.org/browse/JENKINS-57668) - Implement Cloud API to provision Kafka agents

# 2.0.0-alpha

Release date: July 11, 2019

* [JENKINS-57667](https://issues.jenkins-ci.org/browse/JENKINS-57667), [JENKINS-57896](https://issues.jenkins-ci.org/browse/JENKINS-57896) - Launching Zookeeper and Kafka in Kubernetes feature
* [JENKINS-57669](https://issues.jenkins-ci.org/browse/JENKINS-57669) - Kubernetes YAML specification files for Zookeeper and Kafka

# 1.1.3

Release date: Oct 20, 2018

Bug fix:

* [JENKINS-54130](https://issues.jenkins-ci.org/browse/JENKINS-54130) - Fix Remoting Kafka agent docker build.

# 1.1.1

Release date: Oct 03, 2018

Upgrade:

* [JENKINS-53416](https://issues.jenkins-ci.org/browse/JENKINS-53416) - Upgrade remoting to 3.26 and core to 2.138.1.

Bug fix:

* [JENKINS-53397](https://issues.jenkins-ci.org/browse/JENKINS-53397) - Fix ChannelClosedException when using kafka nodes.

# 1.1

Release date: Aug 12, 2018

Enhancement:

* [JENKINS-52079](https://issues.jenkins-ci.org/browse/JENKINS-52079) - Tear down connection when master or agent stops.

Documentation:

* [JENKINS-52514](https://issues.jenkins-ci.org/browse/JENKINS-52514) - Add technical documentation of the plugin.
* [JENKINS-52987](https://issues.jenkins-ci.org/browse/JENKINS-52987) - Update demo instruction to use the plugin without Kafka-enabled SSL.

Test automation:

* [JENKINS-51714](https://issues.jenkins-ci.org/browse/JENKINS-51714) - Basic test automation for the plugin.
* [JENKINS-52879](https://issues.jenkins-ci.org/browse/JENKINS-52879) - Explore testcontainers for test automation.

# 1.0

Release date: Jul 18, 2018

Enhancement:

* [JENKINS-52044](https://issues.jenkins-ci.org/browse/JENKINS-52044) - Remove JNLP agent port from the Remoting Kafka Plugin demo.
* [JENKINS-51471](https://issues.jenkins-ci.org/browse/JENKINS-51471) - Support configuring Remoting Kafka Global config and agents via Configuration-as-Code plugin.
* [JENKINS-52422](https://issues.jenkins-ci.org/browse/JENKINS-52422) - Remove diagnostics logging of events.
* [JENKINS-51711](https://issues.jenkins-ci.org/browse/JENKINS-51711) - Ready-to-fly demo for Remoting Kafka.

# 1.0.0-alpha-2

Release date: Jul 12, 2018

Features:

* [JENKINS-51472](https://issues.jenkins-ci.org/browse/JENKINS-51472) - Remoting Kafka plugin should support Kafka authorization.
* [JENKINS-51473](https://issues.jenkins-ci.org/browse/JENKINS-51473) - Remoting Kafka client should support Kafka authorization.
* [JENKINS-51830](https://issues.jenkins-ci.org/browse/JENKINS-51830) - Add "Test Connection" button to Global Kafka configurations.
* [JENKINS-52200](https://issues.jenkins-ci.org/browse/JENKINS-52200) - Integrate Kafka admin portal.

Fixed issues:

* [JENKINS-52343](https://issues.jenkins-ci.org/browse/JENKINS-52200) - Fix exceptions of closeRead() method in KafkaClassicCommandTransport.

# 1.0.0-alpha-1

Release date: Jun 28, 2018

Features:

* [JENKINS-51414](https://issues.jenkins-ci.org/browse/JENKINS-51414) - Implementation of command transport for command invocation (for master).
* [JENKINS-51708](https://issues.jenkins-ci.org/browse/JENKINS-51708) - Implementation of command transport for command invocation (for agents).
* [JENKINS-51470](https://issues.jenkins-ci.org/browse/JENKINS-51470) - Remoting Kafka agents should provide connection security.
* [JENKINS-51857](https://issues.jenkins-ci.org/browse/JENKINS-51857) - Stop storing Kafka Properties on disk.
* [JENKINS-52190](https://issues.jenkins-ci.org/browse/JENKINS-52190) - Update Remoting Kafka to the released versions of Remoting/Core with extensibility API.
* [JENKINS-51942](https://issues.jenkins-ci.org/browse/JENKINS-51942) - Improve Kafka producer-consumer topic model.
