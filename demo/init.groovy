import hudson.slaves.DumbSlave
import io.jenkins.plugins.remotingkafka.KafkaComputerLauncher
import io.jenkins.plugins.remotingkafka.KafkaSecretManager
import jenkins.model.Jenkins
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition
import org.jenkinsci.plugins.workflow.job.WorkflowJob

import javax.crypto.spec.SecretKeySpec

println("-- Configuring the agent");
KafkaSecretManager.AGENT_SECRET.@key = new SecretKeySpec(new byte[10], "HmacSHA256");

def node = new DumbSlave("test", "/home/jenkins", new KafkaComputerLauncher("admin",
        "/kafka.truststore.jks", "/kafka.keystore.jks", "true"));
Jenkins.instance.addNode(node);

println("-- Creating Jobs")
//TODO: Classes do not work here, so some copy-paste for now

if(Jenkins.instance.getItem("Demo_ping") == null) {
    WorkflowJob project1 = Jenkins.instance.createProject(WorkflowJob.class, "Demo_ping")
    project1.definition = new CpsFlowDefinition(
        "node('test') {\n" +
        "  sh \"ping -c 20 google.com\"\n" +
        "}",
        true // Sandbox
    )
    project1.save()
}

if(Jenkins.instance.getItem("Demo_hello") == null) {
    WorkflowJob project2 = Jenkins.instance.createProject(WorkflowJob.class, "Demo_hello")
    project2.definition = new CpsFlowDefinition(
        "node('test') {\n" +
        "  sh \"echo Hello, world!\"\n" +
        "}",
        true // Sandbox
    )
    project2.save()
}
