package org.knullci.knull.domain.model;

import org.junit.jupiter.api.Test;
import org.knullci.knull.domain.enums.Permission;
import org.knullci.knull.domain.enums.Role;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class KnullUserDetailsTest {

    @Test
    void testGetAuthoritiesIncludesRole() {
        User user = new User(
                1L, "testuser", "test@example.com", "password",
                "Test User", Role.DEVELOPER, Set.of(),
                true, false, LocalDateTime.now(), LocalDateTime.now(), null);

        KnullUserDetails userDetails = new KnullUserDetails(user);
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        assertTrue(authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DEVELOPER")));
    }

    @Test
    void testGetAuthoritiesIncludesPermissions() {
        User user = new User(
                1L, "testuser", "test@example.com", "password",
                "Test User", Role.DEVELOPER, Set.of(),
                true, false, LocalDateTime.now(), LocalDateTime.now(), null);

        KnullUserDetails userDetails = new KnullUserDetails(user);
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        // Should include permissions from DEVELOPER role
        assertTrue(authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("JOB_READ")));
        assertTrue(authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("JOB_CREATE")));
    }

    @Test
    void testGetAuthoritiesIncludesAdditionalPermissions() {
        User user = new User(
                1L, "viewer", "viewer@example.com", "password",
                "Viewer User", Role.VIEWER, Set.of(Permission.JOB_CREATE),
                true, false, LocalDateTime.now(), LocalDateTime.now(), null);

        KnullUserDetails userDetails = new KnullUserDetails(user);
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        // Should include additional permission
        assertTrue(authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("JOB_CREATE")));
    }

    @Test
    void testIsAccountNonLocked() {
        User lockedUser = new User(
                1L, "locked", "locked@example.com", "password",
                "Locked User", Role.VIEWER, Set.of(),
                true, true, // accountLocked = true
                LocalDateTime.now(), LocalDateTime.now(), null);

        User unlockedUser = new User(
                2L, "unlocked", "unlocked@example.com", "password",
                "Unlocked User", Role.VIEWER, Set.of(),
                true, false, // accountLocked = false
                LocalDateTime.now(), LocalDateTime.now(), null);

        KnullUserDetails lockedDetails = new KnullUserDetails(lockedUser);
        KnullUserDetails unlockedDetails = new KnullUserDetails(unlockedUser);

        assertFalse(lockedDetails.isAccountNonLocked());
        assertTrue(unlockedDetails.isAccountNonLocked());
    }

    @Test
    void testIsEnabled() {
        User activeUser = new User(
                1L, "active", "active@example.com", "password",
                "Active User", Role.VIEWER, Set.of(),
                true, false, LocalDateTime.now(), LocalDateTime.now(), null);

        User inactiveUser = new User(
                2L, "inactive", "inactive@example.com", "password",
                "Inactive User", Role.VIEWER, Set.of(),
                false, false, // active = false
                LocalDateTime.now(), LocalDateTime.now(), null);

        KnullUserDetails activeDetails = new KnullUserDetails(activeUser);
        KnullUserDetails inactiveDetails = new KnullUserDetails(inactiveUser);

        assertTrue(activeDetails.isEnabled());
        assertFalse(inactiveDetails.isEnabled());
    }

    @Test
    void testGetDisplayName() {
        User userWithDisplayName = new User(
                1L, "user1", "user1@example.com", "password",
                "John Doe", Role.VIEWER, Set.of(),
                true, false, LocalDateTime.now(), LocalDateTime.now(), null);

        KnullUserDetails details = new KnullUserDetails(userWithDisplayName);

        assertEquals("John Doe", details.getDisplayName());
    }

    @Test
    void testHasPermission() {
        User developer = new User(
                1L, "dev", "dev@example.com", "password",
                "Developer", Role.DEVELOPER, Set.of(),
                true, false, LocalDateTime.now(), LocalDateTime.now(), null);

        KnullUserDetails details = new KnullUserDetails(developer);

        assertTrue(details.hasPermission(Permission.JOB_READ));
        assertTrue(details.hasPermission(Permission.JOB_CREATE));
        assertFalse(details.hasPermission(Permission.USER_CREATE));
    }

    @Test
    void testGetRole() {
        User admin = new User(
                1L, "admin", "admin@example.com", "password",
                "Admin", Role.ADMIN, Set.of(),
                true, false, LocalDateTime.now(), LocalDateTime.now(), null);

        KnullUserDetails details = new KnullUserDetails(admin);

        assertEquals(Role.ADMIN, details.getRole());
    }
}
