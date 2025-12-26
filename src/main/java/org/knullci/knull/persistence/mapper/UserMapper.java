package org.knullci.knull.persistence.mapper;

import org.knullci.knull.domain.model.User;

/**
 * Mapper for User domain model to/from persistence entity.
 */
public class UserMapper {

    private UserMapper() {
        // Utility class
    }

    public static User fromEntity(org.knullci.knull.persistence.entity.User entity) {
        if (entity == null)
            return null;

        return new User(
                entity.getId(),
                entity.getUsername(),
                entity.getEmail(),
                entity.getPassword(),
                entity.getDisplayName(),
                entity.getRole(),
                entity.getAdditionalPermissionsSet(),
                entity.isActive(),
                entity.isAccountLocked(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getLastLoginAt());
    }

    public static org.knullci.knull.persistence.entity.User toEntity(User user) {
        if (user == null)
            return null;

        var entity = new org.knullci.knull.persistence.entity.User();
        entity.setId(user.getId());
        entity.setUsername(user.getUsername());
        entity.setEmail(user.getEmail());
        entity.setPassword(user.getPassword());
        entity.setDisplayName(user.getDisplayName());
        entity.setRole(user.getRole());
        entity.setAdditionalPermissionsSet(user.getAdditionalPermissions());
        entity.setActive(user.isActive());
        entity.setAccountLocked(user.isAccountLocked());
        entity.setCreatedAt(user.getCreatedAt());
        entity.setUpdatedAt(user.getUpdatedAt());
        entity.setLastLoginAt(user.getLastLoginAt());

        return entity;
    }
}
