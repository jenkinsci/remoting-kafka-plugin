FROM jenkins/jenkins:2.129
ADD plugin/target/remoting-kafka.hpi /usr/share/jenkins/ref/plugins/remoting-kafka.jpi
ENTRYPOINT ["tini", "--", "/usr/local/bin/jenkins.sh"]
