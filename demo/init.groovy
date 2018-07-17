import io.jenkins.plugins.remotingkafka.KafkaSecretManager
import jenkins.model.Jenkins
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition
import org.jenkinsci.plugins.workflow.job.WorkflowJob

import javax.crypto.spec.SecretKeySpec

// TODO: raise a ticket in configuration-as-code, create a configurator for the secret.
println("-- Configuring the agent secret");
KafkaSecretManager.AGENT_SECRET.@key = new SecretKeySpec(new byte[10], "HmacSHA256");
