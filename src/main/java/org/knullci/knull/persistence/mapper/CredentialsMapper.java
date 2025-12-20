package org.knullci.knull.persistence.mapper;

import org.knullci.knull.domain.enums.CredentialType;
import org.knullci.knull.domain.model.Credentials;
import org.knullci.knull.domain.model.TokenCredential;
import org.knullci.knull.domain.model.UsernamePasswordCredential;

public class CredentialsMapper {
    public static Credentials fromEntity(org.knullci.knull.persistence.entity.Credentials credentials) {
        UsernamePasswordCredential usernamePasswordCredential = null;
        TokenCredential tokenCredential = null;
        
        if (credentials.getUsernamePasswordCredential() != null) {
            usernamePasswordCredential = new UsernamePasswordCredential(
                credentials.getUsernamePasswordCredential().getUsername(),
                credentials.getUsernamePasswordCredential().getEncryptedPassword()
            );
        }
        
        if (credentials.getTokenCredential() != null) {
            tokenCredential = new TokenCredential(
                credentials.getTokenCredential().getEncryptedToken()
            );
        }
        
        return new Credentials(
                credentials.getId(),
                credentials.getName(),
                credentials.getDescription(),
                CredentialType.valueOf(credentials.getCredentialType().name()),
                usernamePasswordCredential,
                tokenCredential,
                UserMapper.fromEntity(credentials.getCreatedBy()),
                credentials.getCreateAt(),
                UserMapper.fromEntity(credentials.getModifiedBy()),
                credentials.getModifiedAt()
        );
    }

    public static org.knullci.knull.persistence.entity.Credentials toEntity(Credentials credentials) {
        org.knullci.knull.persistence.entity.UsernamePasswordCredential usernamePasswordCredential = null;
        org.knullci.knull.persistence.entity.TokenCredential tokenCredential = null;
        
        if (credentials.getUsernamePasswordCredential() != null) {
            usernamePasswordCredential = new org.knullci.knull.persistence.entity.UsernamePasswordCredential(
                credentials.getUsernamePasswordCredential().getUsername(),
                credentials.getUsernamePasswordCredential().getEncryptedPassword()
            );
        }
        
        if (credentials.getTokenCredential() != null) {
            tokenCredential = new org.knullci.knull.persistence.entity.TokenCredential(
                credentials.getTokenCredential().getEncryptedToken()
            );
        }
        
        return new org.knullci.knull.persistence.entity.Credentials(
                credentials.getId(),
                credentials.getName(),
                credentials.getDescription(),
                org.knullci.knull.persistence.enums.CredentialType.valueOf(credentials.getCredentialType().name()),
                usernamePasswordCredential,
                tokenCredential,
                UserMapper.toEntity(credentials.getCreatedBy()),
                credentials.getCreateAt(),
                UserMapper.toEntity(credentials.getModifiedBy()),
                credentials.getModifiedAt()
        );
    }
}
