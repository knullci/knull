package org.knullci.knull.persistence.mapper;

import org.knullci.knull.domain.model.SecretFile;

public class SecretFileMapper {

    public static org.knullci.knull.persistence.entity.SecretFile toEntity(SecretFile secretFile) {
        if (secretFile == null) {
            return null;
        }
        return new org.knullci.knull.persistence.entity.SecretFile(
                secretFile.getId(),
                secretFile.getName(),
                secretFile.getDescription(),
                secretFile.getType() != null ? secretFile.getType().name() : null,
                secretFile.getEncryptedContent(),
                secretFile.getCreatedAt(),
                secretFile.getUpdatedAt());
    }

    public static SecretFile fromEntity(org.knullci.knull.persistence.entity.SecretFile entity) {
        if (entity == null) {
            return null;
        }
        SecretFile secretFile = new SecretFile();
        secretFile.setId(entity.getId());
        secretFile.setName(entity.getName());
        secretFile.setDescription(entity.getDescription());
        secretFile.setType(entity.getType() != null ? SecretFile.SecretType.valueOf(entity.getType()) : null);
        secretFile.setEncryptedContent(entity.getEncryptedContent());
        secretFile.setCreatedAt(entity.getCreatedAt());
        secretFile.setUpdatedAt(entity.getUpdatedAt());
        return secretFile;
    }
}
