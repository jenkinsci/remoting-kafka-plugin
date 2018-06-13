# Remoting Kafka Plugin

## Overview

Current versions of Jenkins Remoting are based on the TCP protocol. If it fails, the agent connection and the build fails as well. There are also issues with traffic prioritization and multi-agent communications, which impact Jenkins stability and scalability.

This project aims an update of Remoting and Jenkins in order to add support of a popular message queue/bus technology (Kafka) as a fault-tolerant communication layer in Jenkins.

More information about this project can be found at: https://jenkins.io/projects/gsoc/2018/remoting-over-message-bus/

## Requirement
docker, docker-compose installed

## Plugin demo instructions

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

    - Consumer Group ID

    5.2. Create an agent named test2 with Kafka option.

    5.3. Execute jobs to see how it works in the remote agent over Kafka.

6. Stop services using Docker Compose

        docker-compose down
