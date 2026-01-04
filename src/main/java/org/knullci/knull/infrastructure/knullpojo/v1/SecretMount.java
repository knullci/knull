package org.knullci.knull.infrastructure.knullpojo.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Represents a secret file mount configuration in a pipeline step.
 * 
 * Example YAML usage:
 * 
 * secrets:
 * - name: my-kubeconfig # Secret name (required)
 * path: /tmp/.kube/config # Mount path (optional, uses secret's mountPath if
 * not specified)
 * env: KUBECONFIG # Optional: also set as environment variable
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SecretMount {

    /**
     * Name of the secret file to mount.
     * Must match a secret file name in the Knull secret store.
     */
    private String name;

    /**
     * Path where the secret file should be written.
     * If not specified, uses the secret's configured mountPath.
     */
    private String path;

    /**
     * Optional environment variable name.
     * If specified, sets this environment variable to the path of the mounted
     * secret.
     * Useful for tools like kubectl that use KUBECONFIG env var.
     */
    private String env;

}
