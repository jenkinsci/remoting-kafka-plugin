FROM jenkins/jenkins:2.138.1
ADD plugin/target/remoting-kafka.hpi /usr/share/jenkins/ref/plugins/remoting-kafka.jpi
USER root
RUN install-plugins.sh credentials:2.1.17 configuration-as-code:0.10-alpha workflow-aggregator:2.5 job-dsl:1.70
USER jenkins
ENTRYPOINT ["tini", "--", "/usr/local/bin/jenkins.sh"]
