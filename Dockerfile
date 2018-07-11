FROM jenkins/jenkins:2.129
ADD plugin/target/remoting-kafka.hpi /usr/share/jenkins/ref/plugins/remoting-kafka.jpi
USER root
RUN install-plugins.sh credentials
USER jenkins
ENTRYPOINT ["tini", "--", "/usr/local/bin/jenkins.sh"]
