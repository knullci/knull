package org.knullci.knull.application.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.knullci.knull.domain.enums.CredentialType;

@Getter
@AllArgsConstructor
public class CreateCredentialCommand {
    private String name;
    private String description;
    private CredentialType credentialType;
    
    // For USERNAME_PASSWORD type
    private String username;
    private String password;
    
    // For TOKEN type
    private String token;
}
