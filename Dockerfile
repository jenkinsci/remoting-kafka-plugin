# build project
FROM maven:3.5.3-jdk-8 as builder
COPY agent/ /jenkins/src/agent/
COPY kafka-client-lib/ /jenkins/src/kafka-client-lib/
COPY plugin/ /jenkins/src/plugin/
COPY pom.xml /jenkins/src/pom.xml

WORKDIR /jenkins/src/
RUN mvn clean install -DskipTests --batch-mode

# copy agent
FROM ubuntu
RUN apt-get update && apt-get install -y software-properties-common
RUN echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | debconf-set-selections && \
    add-apt-repository -y ppa:webupd8team/java && \
    apt-get update && \
    apt-get install -y oracle-java8-installer && \
    apt-get install -y iputils-ping && \
    rm -rf /var/lib/apt/lists/* && \
    rm -rf /var/cache/oracle-jdk8-installer
COPY --from=builder /jenkins/src/agent/target/remoting-kafka-agent.jar remoting-kafka-agent.jar
ENTRYPOINT ["java", "-jar", "remoting-kafka-agent.jar"]
CMD ["-help"]
