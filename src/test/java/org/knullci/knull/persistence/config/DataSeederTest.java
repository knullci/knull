package org.knullci.knull.persistence.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.knullci.knull.domain.model.User;
import org.knullci.knull.domain.repository.UserRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test for DataSeeder.
 * 
 * Note: DataSeeder no longer auto-creates admin users.
 * It only logs a message directing users to /setup page when no users exist.
 * The actual admin user creation is handled by SetupAdminCommandHandler.
 */
@ExtendWith(MockitoExtension.class)
class DataSeederTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DataSeeder dataSeeder;

    @Test
    void testRun_WhenNoUsersExist_ShouldLogSetupMessage() throws Exception {
        // Arrange
        when(userRepository.count()).thenReturn(0L);

        // Act
        dataSeeder.run();

        // Assert - DataSeeder should only check count, not create any users
        verify(userRepository).count();
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRun_WhenUsersExist_ShouldLogSuccessMessage() throws Exception {
        // Arrange
        when(userRepository.count()).thenReturn(5L);

        // Act
        dataSeeder.run();

        // Assert
        verify(userRepository).count();
        verify(userRepository, never()).save(any(User.class));
    }
}
