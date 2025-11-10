package org.knullci.knull.persistence.mapper;

import org.knullci.knull.domain.enums.CredentialType;
import org.knullci.knull.domain.model.Credentials;

public class CredentialsMapper {
    public static Credentials fromEntity(org.knullci.knull.persistence.entity.Credentials credentials) {
        return new Credentials(
                credentials.getId(),
                credentials.getName(),
                credentials.getDescription(),
                CredentialType.valueOf(credentials.getCredentialType().name()),
                UserMapper.fromEntity(credentials.getCreatedBy()),
                credentials.getCreateAt(),
                UserMapper.fromEntity(credentials.getModifiedBy()),
                credentials.getModifiedAt()
        );
    }

    public static org.knullci.knull.persistence.entity.Credentials toEntity(Credentials credentials) {
        return new org.knullci.knull.persistence.entity.Credentials(
                credentials.getId(),
                credentials.getName(),
                credentials.getDescription(),
                org.knullci.knull.persistence.enums.CredentialType.valueOf(credentials.getCredentialType().name()),
                UserMapper.toEntity(credentials.getCreatedBy()),
                credentials.getCreateAt(),
                UserMapper.toEntity(credentials.getModifiedBy()),
                credentials.getModifiedAt()
        );
    }
}
