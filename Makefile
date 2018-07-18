.PHONY: all
all: clean build
clean:
	docker-compose down
build:
	mvn clean install
	./ssl_setup.sh
	docker-compose build
	docker-compose pull
run:
	docker-compose up -d zookeeper kafka jenkins kafka-manager
	echo "Waiting 60s for jenkins to be ready..."
	sleep 60
	docker-compose up -d agent
