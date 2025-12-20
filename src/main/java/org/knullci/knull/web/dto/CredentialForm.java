package org.knullci.knull.web.dto;

import lombok.Data;
import org.knullci.knull.domain.enums.CredentialType;

@Data
public class CredentialForm {
    private String name;
    private String description;
    private CredentialType credentialType;
    
    // For USERNAME_PASSWORD type
    private String username;
    private String password;
    
    // For TOKEN type
    private String token;
}
