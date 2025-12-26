package org.knullci.knull.application.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.knullci.knull.application.dto.SettingsDto;
import org.knullci.knull.application.query.GetSettingsQuery;
import org.knullci.knull.domain.model.Settings;
import org.knullci.knull.domain.repository.SettingsRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetSettingsQueryHandlerImplTest {

    @Mock
    private SettingsRepository settingsRepository;

    @InjectMocks
    private GetSettingsQueryHandlerImpl handler;

    @Test
    void testHandle_WithExistingSettings_ShouldReturnDto() {
        // Arrange
        Settings settings = new Settings(1L, 123L);
        when(settingsRepository.getSettings()).thenReturn(Optional.of(settings));

        // Act
        SettingsDto result = handler.handle(new GetSettingsQuery());

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(123L, result.getGithubCredentialId());
        verify(settingsRepository).getSettings();
    }

    @Test
    void testHandle_WithNoSettings_ShouldReturnDefaultSettings() {
        // Arrange
        when(settingsRepository.getSettings()).thenReturn(Optional.empty());

        // Act
        SettingsDto result = handler.handle(new GetSettingsQuery());

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId()); // Default ID is 1
        assertNull(result.getGithubCredentialId()); // Default credential ID is null
        verify(settingsRepository).getSettings();
    }

    @Test
    void testHandle_WithSettingsHavingNullCredential_ShouldReturnNullCredentialId() {
        // Arrange
        Settings settings = new Settings(1L, null);
        when(settingsRepository.getSettings()).thenReturn(Optional.of(settings));

        // Act
        SettingsDto result = handler.handle(new GetSettingsQuery());

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertNull(result.getGithubCredentialId());
    }

    @Test
    void testHandle_ShouldCallRepositoryOnce() {
        // Arrange
        when(settingsRepository.getSettings()).thenReturn(Optional.empty());

        // Act
        handler.handle(new GetSettingsQuery());

        // Assert
        verify(settingsRepository, times(1)).getSettings();
    }
}
