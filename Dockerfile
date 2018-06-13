FROM jenkins/jenkins:2.121.1
ADD jenkins.war /usr/share/jenkins/jenkins.war
ADD plugin/target/remoting-kafka.hpi /usr/share/jenkins/ref/plugins/remoting-kafka.jpi
ENTRYPOINT ["tini", "--", "/usr/local/bin/jenkins.sh"]
