#!/usr/bin/env bash
# The script is used for manual testing of the agent in debug mode.
mvn exec:java -Dexec.mainClass="io.jenkins.plugins.remotingkafka.Agent" -Dexec.args="-name test2 -master http://localhost:9090 -kafkaURL 127.0.0.1:9092"
