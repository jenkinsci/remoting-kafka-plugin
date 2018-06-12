#!/usr/bin/env bash
# The script is used for manual testing of the agent.
extra_java_opts=()

if [[ "$DEBUG" ]] ; then
  extra_java_opts+=( \
    '-Xdebug' \
    '-Xrunjdwp:server=y,transport=dt_socket,address=5005,suspend=y' \
  )
fi

java ${extra_java_opts[@]} -jar target/remoting-kafka-agent-1.0-SNAPSHOT-jar-with-dependencies.jar -name test2 -master http://localhost:8080 -kafkaURL 127.0.0.1:9092
