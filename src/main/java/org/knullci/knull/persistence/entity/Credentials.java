package org.knullci.knull.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.knullci.knull.persistence.enums.CredentialType;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Credentials {

    private Long id;

    private String name;

    private String description;

    private CredentialType credentialType;
    
    // Credential data - only one will be populated based on credentialType
    private UsernamePasswordCredential usernamePasswordCredential;
    
    private TokenCredential tokenCredential;

    private User createdBy;

    private Date createAt;

    private User modifiedBy;

    private Date modifiedAt;

}
