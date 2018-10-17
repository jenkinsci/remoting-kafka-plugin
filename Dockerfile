# build project
FROM maven:3.5.3-jdk-8 as builder
COPY agent/ /jenkins/src/agent/
COPY kafka-client-lib/ /jenkins/src/kafka-client-lib/
COPY plugin/ /jenkins/src/plugin/
COPY pom.xml /jenkins/src/pom.xml

WORKDIR /jenkins/src/
RUN mvn clean -Dtest=\!KafkaComputerLauncherTest -DfailIfNoTests=false install --batch-mode

# Install OpenJDK-8
FROM ubuntu:18.04
RUN apt-get update && \
    apt-get install -y openjdk-8-jdk && \
    apt-get install -y ant && \
    apt-get clean;

# Fix certificate issues
RUN apt-get update && \
    apt-get install ca-certificates-java && \
    apt-get clean && \
    update-ca-certificates -f;

# Setup JAVA_HOME -- useful for docker commandline
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64/
RUN export JAVA_HOME

# Install ping utils
RUN apt-get install -y iputils-ping

# Copy agent
COPY --from=builder /jenkins/src/agent/target/remoting-kafka-agent.jar remoting-kafka-agent.jar
ENTRYPOINT ["java", "-jar", "remoting-kafka-agent.jar"]
CMD ["-help"]
