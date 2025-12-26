package org.knullci.knull.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.knullci.knull.domain.enums.Permission;
import org.knullci.knull.domain.enums.Role;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Domain model for User with RBAC support.
 * Immutable - use builder or factory for updates.
 */
@Getter
@AllArgsConstructor
public class User {
    private final Long id;
    private final String username;
    private final String email;
    private final String password;
    private final String displayName;
    private final Role role;
    private final Set<Permission> additionalPermissions;
    private final boolean active;
    private final boolean accountLocked;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime lastLoginAt;

    /**
     * Constructor for backward compatibility
     */
    public User(String username, String password) {
        this.id = null;
        this.username = username;
        this.email = null;
        this.password = password;
        this.displayName = username;
        this.role = Role.VIEWER;
        this.additionalPermissions = Set.of();
        this.active = true;
        this.accountLocked = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.lastLoginAt = null;
    }

    /**
     * Constructor for backward compatibility with ID
     */
    public User(Long id, String username, String password) {
        this.id = id;
        this.username = username;
        this.email = null;
        this.password = password;
        this.displayName = username;
        this.role = Role.VIEWER;
        this.additionalPermissions = Set.of();
        this.active = true;
        this.accountLocked = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.lastLoginAt = null;
    }

    /**
     * Check if user has a specific permission (either from role or additional
     * permissions)
     */
    public boolean hasPermission(Permission permission) {
        if (additionalPermissions != null && additionalPermissions.contains(permission)) {
            return true;
        }
        return RolePermissions.hasPermission(role, permission);
    }

    /**
     * Check if user has a specific role or higher
     */
    public boolean hasRole(Role requiredRole) {
        return role.ordinal() <= requiredRole.ordinal();
    }

    /**
     * Check if user is an admin
     */
    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
}
