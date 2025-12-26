package org.knullci.knull.persistence.mapper;

import org.junit.jupiter.api.Test;
import org.knullci.knull.domain.enums.Permission;
import org.knullci.knull.domain.enums.Role;
import org.knullci.knull.domain.model.User;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    @Test
    void testFromEntity_ShouldMapAllFields() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        org.knullci.knull.persistence.entity.User entity = new org.knullci.knull.persistence.entity.User();
        entity.setId(1L);
        entity.setUsername("testuser");
        entity.setEmail("test@example.com");
        entity.setPassword("password123");
        entity.setDisplayName("Test User");
        entity.setRole(Role.DEVELOPER);
        entity.setAdditionalPermissions("JOB_CREATE,JOB_DELETE");
        entity.setActive(true);
        entity.setAccountLocked(false);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setLastLoginAt(now);

        // Act
        User result = UserMapper.fromEntity(entity);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("password123", result.getPassword());
        assertEquals("Test User", result.getDisplayName());
        assertEquals(Role.DEVELOPER, result.getRole());
        assertTrue(result.getAdditionalPermissions().contains(Permission.JOB_CREATE));
        assertTrue(result.getAdditionalPermissions().contains(Permission.JOB_DELETE));
        assertTrue(result.isActive());
        assertFalse(result.isAccountLocked());
    }

    @Test
    void testFromEntity_WithNullEntity_ShouldReturnNull() {
        assertNull(UserMapper.fromEntity(null));
    }

    @Test
    void testToEntity_ShouldMapAllFields() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        User domainUser = new User(
                1L, "testuser", "test@example.com", "password123",
                "Test User", Role.DEVELOPER, Set.of(Permission.JOB_CREATE),
                true, false, now, now, now);

        // Act
        org.knullci.knull.persistence.entity.User result = UserMapper.toEntity(domainUser);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("password123", result.getPassword());
        assertEquals("Test User", result.getDisplayName());
        assertEquals(Role.DEVELOPER, result.getRole());
        assertTrue(result.getAdditionalPermissions().contains("JOB_CREATE"));
        assertTrue(result.isActive());
        assertFalse(result.isAccountLocked());
    }

    @Test
    void testToEntity_WithNullUser_ShouldReturnNull() {
        assertNull(UserMapper.toEntity(null));
    }

    @Test
    void testFromEntity_WithEmptyAdditionalPermissions_ShouldReturnEmptySet() {
        // Arrange
        org.knullci.knull.persistence.entity.User entity = new org.knullci.knull.persistence.entity.User();
        entity.setId(1L);
        entity.setUsername("testuser");
        entity.setRole(Role.VIEWER);
        entity.setAdditionalPermissions(null);

        // Act
        User result = UserMapper.fromEntity(entity);

        // Assert
        assertNotNull(result.getAdditionalPermissions());
        assertTrue(result.getAdditionalPermissions().isEmpty());
    }

    @Test
    void testRoundTrip_ShouldPreserveData() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        User original = new User(
                1L, "testuser", "test@example.com", "password123",
                "Test User", Role.ADMIN, Set.of(Permission.SYSTEM_ADMIN),
                true, false, now, now, null);

        // Act
        org.knullci.knull.persistence.entity.User entity = UserMapper.toEntity(original);
        User result = UserMapper.fromEntity(entity);

        // Assert
        assertEquals(original.getId(), result.getId());
        assertEquals(original.getUsername(), result.getUsername());
        assertEquals(original.getEmail(), result.getEmail());
        assertEquals(original.getPassword(), result.getPassword());
        assertEquals(original.getDisplayName(), result.getDisplayName());
        assertEquals(original.getRole(), result.getRole());
        assertEquals(original.isActive(), result.isActive());
        assertEquals(original.isAccountLocked(), result.isAccountLocked());
    }
}
