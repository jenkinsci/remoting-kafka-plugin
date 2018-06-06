#!/usr/bin/env bash
# The script is used for manual testing of the agent.
java -jar target/remoting-kafka-agent-1.0-SNAPSHOT-jar-with-dependencies.jar -name test -master http://localhost:9090 -kafkaURL 127.0.0.1:9092
