# Remoting Kafka Plugin
This project is currently under development.

## Overview

Current versions of Jenkins Remoting are based on the TCP protocol. If it fails, the agent connection and the build fails as well. There are also issues with traffic prioritization and multi-agent communications, which impact Jenkins stability and scalability.

This project aims an update of Remoting and Jenkins in order to add support of a popular message queue/bus technology (Kafka) as a fault-tolerant communication layer in Jenkins.

More information about this project can be found at: https://jenkins.io/projects/gsoc/2018/remoting-over-message-bus/

## Release Notes

See the [Changelog](CHANGELOG.md).

## How to use the plugin in alpha version

Alpha version of the plugin is now released under [Experimental Update Center](https://jenkins.io/doc/developer/publishing/releasing-experimental-updates/#configuring-jenkins-to-use-experimental-update-center).

Requirements to use the plugin under alpha released:

1. You must have a Kafka cluster running (with host and port). If not, you can up Kafka and Zookeeper services using `docker-compose.yml` in this repository using command `docker-compose up -d`.

2. You must have a custom agent JAR to use this plugin. The JAR can be downloaded from [Jenkins artifactory](https://repo.jenkins-ci.org/releases/io/jenkins/plugins/remoting-kafka/remoting-kafka-agent/1.0.0-alpha-1/remoting-kafka-agent-1.0.0-alpha-1.jar
).

3. The instruction to run the plugin is similar to what has been described in this [blogpost](https://jenkins.io/blog/2018/06/18/remoting-over-message-bus/) with some updates about security features (see [Changelog](CHANGELOG.md)). You need to:

- Config Kafka and Zookeeper address in Global System configuration.
- Start an agent from UI (Launch agents with Kafka) then copy the command line string to start your agent in remote machine (similar to JNLP agent). For example:

        java -jar remoting-kafka-agent.jar -name test -master http://localhost:8080/ -secret a770e457a8cd2316a5dcee9e8c0869b8efd2b57403827fb7ce47206a9df4122e -kafkaURL 172.17.0.1:9092

## Plugin demo instructions (in demo branch)

Requirement: docker, docker-compose installed

1. Set docker host environment variable

        export DOCKERHOST=$(ifconfig docker0 | grep 'inet addr:' | cut -d: -f2 | awk '{ print $1}')

2. Download Jenkins war file

        wget https://repo.jenkins-ci.org/snapshots/org/jenkins-ci/main/jenkins-war/2.127-SNAPSHOT/jenkins-war-2.127-20180610.103343-1.war -O jenkins.war

3. Build the Docker images

        docker-compose build

4. Start services using Docker Compose

        docker-compose up -d

5. Go to Jenkins localhost:8080

    5.1. Setup Kafka configuration in Manage Jenkins/Configure System:

    - Connection URL: kafka:9092

    5.2a. Create an agent named test with Kafka option.

    5.2b. Or you can create your own custom agent name with the following command (see help or check agent/run.sh to see an example):

        docker-compose run remoting-kafka-agent --help

    5.3. Execute jobs to see how it works in the remote agent over Kafka.

6. Stop services using Docker Compose

        docker-compose down

## Links

- [Wiki](https://wiki.jenkins.io/display/JENKINS/Remoting+Kafka+Plugin)
- [Project Info](https://jenkins.io/projects/gsoc/2018/remoting-over-message-bus/)
- [Introduction Blogpost](https://jenkins.io/blog/2018/06/18/remoting-over-message-bus/)
- [Gitter Chat](https://gitter.im/jenkinsci/remoting)
