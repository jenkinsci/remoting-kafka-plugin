#!/bin/bash
# Bash script to run demo of the plugin
docker-compose up -d zookeeper kafka jenkins kafka-manager
echo "Waiting 60s for jenkins to be ready..."
sleep 60
docker-compose up -d agent
