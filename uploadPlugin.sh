#!/usr/bin/env bash
/usr/bin/curl -X POST -i -F file=@./plugin/target/remoting-kafka.hpi http://127.0.0.1:8080/pluginManager/uploadPlugin