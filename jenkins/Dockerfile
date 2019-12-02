FROM jenkins/jenkins:2.176.2
ADD plugin/target/remoting-kafka.hpi /usr/share/jenkins/ref/plugins/remoting-kafka.jpi
USER root
RUN install-plugins.sh credentials:2.2.0 kubernetes-credentials:0.4.0 configuration-as-code:1.24 workflow-aggregator:2.6 job-dsl:1.74
USER jenkins
ENTRYPOINT ["tini", "--", "/usr/local/bin/jenkins.sh"]
