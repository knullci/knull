package org.knullci.knull.application.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.knullci.knull.application.command.CreateUserCommand;
import org.knullci.knull.application.dto.UserDto;
import org.knullci.knull.domain.enums.Permission;
import org.knullci.knull.domain.enums.Role;
import org.knullci.knull.domain.model.User;
import org.knullci.knull.domain.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateUserCommandHandlerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CreateUserCommandHandler handler;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Test
    void testHandle_ShouldCreateUserWithEncodedPassword() {
        // Arrange
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        CreateUserCommand command = new CreateUserCommand(
                "newuser", "newuser@example.com", "password123",
                "New User", Role.DEVELOPER, null);

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return new User(1L, user.getUsername(), user.getEmail(), user.getPassword(),
                    user.getDisplayName(), user.getRole(), user.getAdditionalPermissions(),
                    user.isActive(), user.isAccountLocked(), user.getCreatedAt(),
                    user.getUpdatedAt(), user.getLastLoginAt());
        });

        // Act
        UserDto result = handler.handle(command);

        // Assert
        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals("newuser@example.com", result.getEmail());
        assertEquals("New User", result.getDisplayName());
        assertEquals(Role.DEVELOPER, result.getRole());

        verify(passwordEncoder).encode("password123");
        User savedUser = userCaptor.getValue();
        assertEquals("encoded-password", savedUser.getPassword());
    }

    @Test
    void testHandle_WhenUsernameExists_ShouldThrowException() {
        // Arrange
        CreateUserCommand command = new CreateUserCommand(
                "existinguser", "email@example.com", "password",
                "Existing User", Role.VIEWER, null);

        when(userRepository.findByUsername("existinguser")).thenReturn(
                Optional.of(createMockUser("existinguser")));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testHandle_WithNullRole_ShouldDefaultToViewer() {
        // Arrange
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        CreateUserCommand command = new CreateUserCommand(
                "newuser", "email@example.com", "password",
                "New User", null, null);

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return new User(1L, user.getUsername(), user.getEmail(), user.getPassword(),
                    user.getDisplayName(), user.getRole(), user.getAdditionalPermissions(),
                    user.isActive(), user.isAccountLocked(), user.getCreatedAt(),
                    user.getUpdatedAt(), user.getLastLoginAt());
        });

        // Act
        handler.handle(command);

        // Assert
        User savedUser = userCaptor.getValue();
        assertEquals(Role.VIEWER, savedUser.getRole());
    }

    @Test
    void testHandle_WithAdditionalPermissions_ShouldSavePermissions() {
        // Arrange
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        Set<Permission> additionalPerms = Set.of(Permission.SYSTEM_ADMIN);
        CreateUserCommand command = new CreateUserCommand(
                "newuser", "email@example.com", "password",
                "New User", Role.DEVELOPER, additionalPerms);

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return new User(1L, user.getUsername(), user.getEmail(), user.getPassword(),
                    user.getDisplayName(), user.getRole(), user.getAdditionalPermissions(),
                    user.isActive(), user.isAccountLocked(), user.getCreatedAt(),
                    user.getUpdatedAt(), user.getLastLoginAt());
        });

        // Act
        handler.handle(command);

        // Assert
        User savedUser = userCaptor.getValue();
        assertTrue(savedUser.getAdditionalPermissions().contains(Permission.SYSTEM_ADMIN));
    }

    private User createMockUser(String username) {
        return new User(1L, username, "email@example.com", "password",
                username, Role.VIEWER, Set.of(), true, false,
                LocalDateTime.now(), LocalDateTime.now(), null);
    }
}
