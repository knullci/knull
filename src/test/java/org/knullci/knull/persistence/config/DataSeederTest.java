package org.knullci.knull.persistence.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.knullci.knull.domain.enums.Role;
import org.knullci.knull.domain.model.User;
import org.knullci.knull.domain.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataSeederTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DataSeeder dataSeeder;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Test
    void testRun_WhenNoUsersExist_ShouldCreateAdminUser() throws Exception {
        // Arrange
        when(userRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode("knull")).thenReturn("encoded-password");
        when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        dataSeeder.run();

        // Assert
        verify(userRepository).save(any(User.class));

        User createdUser = userCaptor.getValue();
        assertEquals("knull", createdUser.getUsername());
        assertEquals("encoded-password", createdUser.getPassword());
        assertEquals(Role.ADMIN, createdUser.getRole());
        assertEquals("Knull Admin", createdUser.getDisplayName());
        assertTrue(createdUser.isActive());
        assertFalse(createdUser.isAccountLocked());
    }

    @Test
    void testRun_WhenUsersExist_ShouldNotCreateUser() throws Exception {
        // Arrange
        when(userRepository.count()).thenReturn(5L);

        // Act
        dataSeeder.run();

        // Assert
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void testRun_ShouldEncodePasswordBcrypt() throws Exception {
        // Arrange
        when(userRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode("knull")).thenReturn("$2a$10$encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        dataSeeder.run();

        // Assert
        verify(passwordEncoder).encode("knull");
    }
}
