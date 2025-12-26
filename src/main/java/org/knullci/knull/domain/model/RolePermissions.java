package org.knullci.knull.domain.model;

import org.knullci.knull.domain.enums.Permission;
import org.knullci.knull.domain.enums.Role;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Maps roles to their default permissions.
 * This class defines which permissions each role has by default.
 */
public final class RolePermissions {

    private static final Map<Role, Set<Permission>> ROLE_PERMISSIONS = Map.of(
            Role.ADMIN, EnumSet.allOf(Permission.class),

            Role.DEVELOPER, EnumSet.of(
                    // Job permissions
                    Permission.JOB_READ,
                    Permission.JOB_CREATE,
                    Permission.JOB_UPDATE,
                    Permission.JOB_DELETE,
                    Permission.JOB_EXECUTE,
                    // Build permissions
                    Permission.BUILD_READ,
                    Permission.BUILD_CANCEL,
                    // Credential permissions (read-only)
                    Permission.CREDENTIAL_READ,
                    Permission.CREDENTIAL_CREATE,
                    Permission.CREDENTIAL_UPDATE,
                    // Secret permissions
                    Permission.SECRET_READ,
                    Permission.SECRET_CREATE,
                    Permission.SECRET_UPDATE),

            Role.VIEWER, EnumSet.of(
                    Permission.JOB_READ,
                    Permission.BUILD_READ,
                    Permission.CREDENTIAL_READ,
                    Permission.SECRET_READ,
                    Permission.SETTINGS_READ));

    private RolePermissions() {
        // Utility class
    }

    /**
     * Get all permissions for a role
     */
    public static Set<Permission> getPermissions(Role role) {
        return ROLE_PERMISSIONS.getOrDefault(role, EnumSet.noneOf(Permission.class));
    }

    /**
     * Check if a role has a specific permission
     */
    public static boolean hasPermission(Role role, Permission permission) {
        Set<Permission> permissions = ROLE_PERMISSIONS.get(role);
        return permissions != null && permissions.contains(permission);
    }
}
