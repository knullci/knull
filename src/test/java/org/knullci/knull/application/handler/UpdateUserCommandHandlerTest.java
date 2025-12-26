package org.knullci.knull.application.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.knullci.knull.application.command.UpdateUserCommand;
import org.knullci.knull.application.dto.UserDto;
import org.knullci.knull.domain.enums.Permission;
import org.knullci.knull.domain.enums.Role;
import org.knullci.knull.domain.model.User;
import org.knullci.knull.domain.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateUserCommandHandlerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UpdateUserCommandHandler handler;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Test
    void testHandle_ShouldUpdateUserRole() {
        // Arrange
        User existingUser = createMockUser(1L, "testuser", Role.VIEWER);
        UpdateUserCommand command = new UpdateUserCommand(
                1L, "updated@example.com", "Updated User",
                Role.DEVELOPER, null, true, false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserDto result = handler.handle(command);

        // Assert
        assertNotNull(result);
        assertEquals(Role.DEVELOPER, result.getRole());
        assertEquals("Updated User", result.getDisplayName());
        assertEquals("updated@example.com", result.getEmail());

        User savedUser = userCaptor.getValue();
        assertEquals(Role.DEVELOPER, savedUser.getRole());
    }

    @Test
    void testHandle_WhenUserNotFound_ShouldThrowException() {
        // Arrange
        UpdateUserCommand command = new UpdateUserCommand(
                999L, "email@example.com", "User",
                Role.DEVELOPER, null, true, false);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testHandle_ShouldPreserveUsername() {
        // Arrange
        User existingUser = createMockUser(1L, "originaluser", Role.VIEWER);
        UpdateUserCommand command = new UpdateUserCommand(
                1L, "updated@example.com", "Updated",
                Role.DEVELOPER, null, true, false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        handler.handle(command);

        // Assert - Username should not change
        User savedUser = userCaptor.getValue();
        assertEquals("originaluser", savedUser.getUsername());
    }

    @Test
    void testHandle_ShouldUpdateLockStatus() {
        // Arrange
        User existingUser = createMockUser(1L, "testuser", Role.VIEWER);
        UpdateUserCommand command = new UpdateUserCommand(
                1L, "email@example.com", "User",
                Role.VIEWER, null, true, true // Lock the account
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserDto result = handler.handle(command);

        // Assert
        assertTrue(result.isAccountLocked());
    }

    @Test
    void testHandle_WithAdditionalPermissions_ShouldUpdate() {
        // Arrange
        User existingUser = createMockUser(1L, "testuser", Role.VIEWER);
        Set<Permission> newPermissions = Set.of(Permission.JOB_CREATE, Permission.JOB_DELETE);
        UpdateUserCommand command = new UpdateUserCommand(
                1L, "email@example.com", "User",
                Role.VIEWER, newPermissions, true, false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        handler.handle(command);

        // Assert
        User savedUser = userCaptor.getValue();
        assertTrue(savedUser.getAdditionalPermissions().contains(Permission.JOB_CREATE));
        assertTrue(savedUser.getAdditionalPermissions().contains(Permission.JOB_DELETE));
    }

    private User createMockUser(Long id, String username, Role role) {
        return new User(id, username, "email@example.com", "password",
                username, role, Set.of(), true, false,
                LocalDateTime.now(), LocalDateTime.now(), null);
    }
}
