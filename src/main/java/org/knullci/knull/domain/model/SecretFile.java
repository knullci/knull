package org.knullci.knull.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SecretFile {

    private Long id;

    private String name;

    private String description;

    private SecretType type;

    private String encryptedContent;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public enum SecretType {
        FILE, // Generic file (e.g., .env, config.json)
        CERTIFICATE, // SSL/TLS certificates
        SSH_KEY, // SSH private keys
        KUBECONFIG, // Kubernetes config
        ENV_FILE // Environment variables file
    }
}
