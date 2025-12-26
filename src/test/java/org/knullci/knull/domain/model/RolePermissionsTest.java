package org.knullci.knull.domain.model;

import org.junit.jupiter.api.Test;
import org.knullci.knull.domain.enums.Permission;
import org.knullci.knull.domain.enums.Role;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RolePermissionsTest {

    @Test
    void testAdminHasAllPermissions() {
        Set<Permission> adminPermissions = RolePermissions.getPermissions(Role.ADMIN);

        // Admin should have all permissions
        for (Permission permission : Permission.values()) {
            assertTrue(adminPermissions.contains(permission),
                    "Admin should have permission: " + permission);
        }
    }

    @Test
    void testDeveloperHasJobPermissions() {
        assertTrue(RolePermissions.hasPermission(Role.DEVELOPER, Permission.JOB_READ));
        assertTrue(RolePermissions.hasPermission(Role.DEVELOPER, Permission.JOB_CREATE));
        assertTrue(RolePermissions.hasPermission(Role.DEVELOPER, Permission.JOB_UPDATE));
        assertTrue(RolePermissions.hasPermission(Role.DEVELOPER, Permission.JOB_DELETE));
        assertTrue(RolePermissions.hasPermission(Role.DEVELOPER, Permission.JOB_EXECUTE));
    }

    @Test
    void testDeveloperCannotManageUsers() {
        assertFalse(RolePermissions.hasPermission(Role.DEVELOPER, Permission.USER_CREATE));
        assertFalse(RolePermissions.hasPermission(Role.DEVELOPER, Permission.USER_UPDATE));
        assertFalse(RolePermissions.hasPermission(Role.DEVELOPER, Permission.USER_DELETE));
    }

    @Test
    void testViewerHasReadOnlyPermissions() {
        assertTrue(RolePermissions.hasPermission(Role.VIEWER, Permission.JOB_READ));
        assertTrue(RolePermissions.hasPermission(Role.VIEWER, Permission.BUILD_READ));
        assertTrue(RolePermissions.hasPermission(Role.VIEWER, Permission.CREDENTIAL_READ));
        assertTrue(RolePermissions.hasPermission(Role.VIEWER, Permission.SECRET_READ));
        assertTrue(RolePermissions.hasPermission(Role.VIEWER, Permission.SETTINGS_READ));
    }

    @Test
    void testViewerCannotCreateOrModify() {
        assertFalse(RolePermissions.hasPermission(Role.VIEWER, Permission.JOB_CREATE));
        assertFalse(RolePermissions.hasPermission(Role.VIEWER, Permission.JOB_UPDATE));
        assertFalse(RolePermissions.hasPermission(Role.VIEWER, Permission.JOB_DELETE));
        assertFalse(RolePermissions.hasPermission(Role.VIEWER, Permission.JOB_EXECUTE));
    }

    @Test
    void testGetPermissionsReturnsNonNull() {
        for (Role role : Role.values()) {
            assertNotNull(RolePermissions.getPermissions(role));
        }
    }
}
