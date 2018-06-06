#!/usr/bin/env bash
# Commands used in Kafka.

# Delete a topic:
kafka-topics.sh --zookeeper zookeeper:2181 --delete --topic test

# Create a topic:
kafka-topics.sh --create --zookeeper zookeeper:2181 \
    --replication-factor 1 --partitions 1 --topic test

# Consumer console:
kafka-console-consumer.sh \
    --bootstrap-server localhost:9092 \
    --topic my-topic \
    --from-beginning
