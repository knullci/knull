package org.knullci.knull.infrastructure.knullpojo.v1;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JobStep {

    private String name;

    @JsonAlias("command")
    private RunCommand run;

    /**
     * List of secret file references to mount for this step.
     * Each entry can be:
     * - Just the secret name: uses the secret's configured mountPath
     * - A map with "name" and "path": mounts to custom path
     * 
     * Example YAML:
     * secrets:
     * - name: my-kubeconfig
     * path: /tmp/.kube/config
     * - name: my-ssh-key
     */
    private List<SecretMount> secrets;

    /**
     * Environment variables to set for this step.
     * 
     * Example YAML:
     * env:
     * KUBECONFIG: /tmp/.kube/config
     * MY_VAR: value
     */
    private Map<String, String> env;

}
