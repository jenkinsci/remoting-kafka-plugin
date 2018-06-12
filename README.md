# Remoting Kafka Plugin

## Overview

Current versions of Jenkins Remoting are based on the TCP protocol. If it fails, the agent connection and the build fails as well. There are also issues with traffic prioritization and multi-agent communications, which impact Jenkins stability and scalability.

This project aims an update of Remoting and Jenkins in order to add support of a popular message queue/bus technology (Kafka) as a fault-tolerant communication layer in Jenkins.

More information about this project can be found at: https://jenkins.io/projects/gsoc/2018/remoting-over-message-bus/

## Requirement
docker, docker-compose

## Instructions
export DOCKERHOST=$(ifconfig docker0 | grep 'inet addr:' | cut -d: -f2 | awk '{ print $1}')