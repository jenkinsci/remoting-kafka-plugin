FROM jenkins/jenkins:latest
ADD plugin/target/remoting-kafka.hpi /usr/share/jenkins/ref/plugins/remoting-kafka.jpi
USER root
RUN install-plugins.sh credentials
RUN install-plugins.sh configuration-as-code
RUN install-plugins.sh workflow-aggregator
USER jenkins
ENTRYPOINT ["tini", "--", "/usr/local/bin/jenkins.sh"]
