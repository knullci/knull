package org.knullci.knull.application.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateSecretFileCommand {

    private String name;

    private String description;

    private String type; // FILE, CERTIFICATE, SSH_KEY, KUBECONFIG, ENV_FILE

    private String content; // Plain text content (will be encrypted)
}
