.PHONY: all
all: clean build
clean:
	docker-compose down
build:
	mvn clean install -DskipTests
	./ssl_setup.sh
	docker-compose build
	docker-compose pull
run:
	docker-compose up -d zookeeper kafka jenkins kafka-manager
	echo "Waiting 60s for jenkins to be ready..."
	sleep 60
	docker-compose up -d agent

run-no-auth:
	docker-compose up -d zoo2 kafka-no-auth jenkins-no-auth kafka-manager-no-auth
	echo "Waiting 60s for jenkins to be ready..."
	sleep 60
	docker-compose up -d agent-no-auth
