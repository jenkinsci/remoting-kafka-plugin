# Remoting Kafka Plugin

[![Build Status](https://ci.jenkins.io/job/Plugins/job/remoting-kafka-plugin/job/master/badge/icon)](https://ci.jenkins.io/job/Plugins/job/remoting-kafka-plugin/job/master/)
[![Join the chat at https://gitter.im/jenkinsci/remoting](https://badges.gitter.im/jenkinsci/remoting.svg)](https://gitter.im/jenkinsci/remoting?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

This plugin can be found in [Jenkins Plugin](https://plugins.jenkins.io/remoting-kafka).

## Overview

Current versions of Jenkins Remoting are based on the TCP protocol. If it fails, the agent connection and the build fails as well. There are also issues with traffic prioritization and multi-agent communications, which impact Jenkins stability and scalability.

This project aims an update of Remoting and Jenkins in order to add support of a popular message queue/bus technology (Kafka) as a fault-tolerant communication layer in Jenkins.

More information about this project can be found at: https://jenkins.io/projects/gsoc/2018/remoting-over-message-bus/

## Release Notes

See the [CHANGELOG](CHANGELOG.md).

## How to run demo of the plugin

1. Requirements: docker, docker-compose installed.

2. Run `export DOCKERHOST=$(ifconfig docker0 | grep 'inet addr:' | cut -d: -f2 | awk '{ print $1}')`

3. Build the demo: `make all`.

4. Run the demo: `make run`.

5. Features in the demo:

- Docker Compose starts preconfigured Master and agent instance, they connect automatically using Kafka launcher.
- Kafka is secured and encrypted with SSL.
- There few demo jobs in the instance so that a user can launch a job on the agent.
- Kakfa Manager supported in localhost:9000 to support monitoring of Kafka cluster.

6. Stop the demo: `make clean`.

## Links

- [Wiki](https://wiki.jenkins.io/display/JENKINS/Remoting+Kafka+Plugin)
- [Project Info](https://jenkins.io/projects/gsoc/2018/remoting-over-message-bus/)
- [Introduction Blogpost](https://jenkins.io/blog/2018/06/18/remoting-over-message-bus/)
- [Phase 1 Evaluation Slides](https://docs.google.com/presentation/d/1GxkI17lZYQ6_pyAOR9sXNXq1K3LwkqjigXdxxf81VkE/edit?usp=sharing)
- [Phase 2 Evaluation Slides](https://docs.google.com/presentation/d/1TW31N-opvoFwSkD-FChhjCsXNWmeDjkecxJv8Lb6X-A/edit?usp=sharing)
- [Phase 1 Evaluation Video](https://youtu.be/qWHM8S0fzUw)
- [Phase 2 Evaluation Video](https://youtu.be/tuTODhJOTBU)
