package org.knullci.knull.domain.model;

import org.junit.jupiter.api.Test;
import org.knullci.knull.domain.enums.Permission;
import org.knullci.knull.domain.enums.Role;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testCreateUserWithAllFields() {
        LocalDateTime now = LocalDateTime.now();
        Set<Permission> additionalPerms = Set.of(Permission.SYSTEM_ADMIN);

        User user = new User(
                1L, "testuser", "test@example.com", "password123",
                "Test User", Role.DEVELOPER, additionalPerms,
                true, false, now, now, null);

        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertEquals("Test User", user.getDisplayName());
        assertEquals(Role.DEVELOPER, user.getRole());
        assertEquals(additionalPerms, user.getAdditionalPermissions());
        assertTrue(user.isActive());
        assertFalse(user.isAccountLocked());
    }

    @Test
    void testCreateUserWithSimpleConstructor() {
        User user = new User("simpleuser", "simplepass");

        assertEquals("simpleuser", user.getUsername());
        assertEquals("simplepass", user.getPassword());
        assertEquals(Role.VIEWER, user.getRole()); // Default role
        assertTrue(user.isActive());
        assertFalse(user.isAccountLocked());
    }

    @Test
    void testHasPermissionFromRole() {
        User admin = new User(
                1L, "admin", "admin@test.com", "pass",
                "Admin", Role.ADMIN, Set.of(),
                true, false, LocalDateTime.now(), LocalDateTime.now(), null);

        // Admin has all permissions from role
        assertTrue(admin.hasPermission(Permission.USER_CREATE));
        assertTrue(admin.hasPermission(Permission.JOB_DELETE));
        assertTrue(admin.hasPermission(Permission.SYSTEM_ADMIN));
    }

    @Test
    void testHasPermissionFromAdditionalPermissions() {
        // Viewer with additional JOB_CREATE permission
        User viewer = new User(
                1L, "viewer", "viewer@test.com", "pass",
                "Viewer", Role.VIEWER, Set.of(Permission.JOB_CREATE),
                true, false, LocalDateTime.now(), LocalDateTime.now(), null);

        // Should have JOB_READ from role
        assertTrue(viewer.hasPermission(Permission.JOB_READ));
        // Should have JOB_CREATE from additional permissions
        assertTrue(viewer.hasPermission(Permission.JOB_CREATE));
        // Should NOT have JOB_DELETE (neither from role nor additional)
        assertFalse(viewer.hasPermission(Permission.JOB_DELETE));
    }

    @Test
    void testHasRole() {
        User developer = new User(
                1L, "dev", "dev@test.com", "pass",
                "Developer", Role.DEVELOPER, Set.of(),
                true, false, LocalDateTime.now(), LocalDateTime.now(), null);

        // Developer has role DEVELOPER
        assertTrue(developer.hasRole(Role.DEVELOPER));
        assertTrue(developer.hasRole(Role.VIEWER)); // Lower roles
        assertFalse(developer.hasRole(Role.ADMIN)); // Higher role
    }

    @Test
    void testIsAdmin() {
        User admin = new User(
                1L, "admin", "admin@test.com", "pass",
                "Admin", Role.ADMIN, Set.of(),
                true, false, LocalDateTime.now(), LocalDateTime.now(), null);

        User developer = new User(
                2L, "dev", "dev@test.com", "pass",
                "Developer", Role.DEVELOPER, Set.of(),
                true, false, LocalDateTime.now(), LocalDateTime.now(), null);

        assertTrue(admin.isAdmin());
        assertFalse(developer.isAdmin());
    }
}
