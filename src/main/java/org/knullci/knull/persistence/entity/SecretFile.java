package org.knullci.knull.persistence.entity;

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

    private String type; // FILE, CERTIFICATE, SSH_KEY, KUBECONFIG, ENV_FILE

    private String encryptedContent;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
