.PHONY: all
all: clean build
clean:
	docker-compose down
	mvn clean install
build:
	./ssl_setup.sh
	docker-compose build
run:
	docker-compose up -d zookeeper kafka jenkins kafka-manager
	echo "Waiting 60s for jenkins to be ready..."
	sleep 60
	docker-compose up -d agent
stop:
	docker-compose down
