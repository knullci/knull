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
        Settings settings = new Settings();
        settings.setId(1L);
        settings.setGithubCredentialId(123L);
        settings.setInstanceName("My CI");
        settings.setTimezone("America/New_York");
        settings.setMaxConcurrentBuilds(10);
        settings.setBuildTimeoutMinutes(120);
        settings.setBuildRetentionDays(60);
        settings.setAutoCleanupWorkspace(true);
        when(settingsRepository.getSettings()).thenReturn(Optional.of(settings));

        // Act
        SettingsDto result = handler.handle(new GetSettingsQuery());

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(123L, result.getGithubCredentialId());
        assertEquals("My CI", result.getInstanceName());
        assertEquals("America/New_York", result.getTimezone());
        assertEquals(10, result.getMaxConcurrentBuilds());
        assertEquals(120, result.getBuildTimeoutMinutes());
        assertEquals(60, result.getBuildRetentionDays());
        assertTrue(result.getAutoCleanupWorkspace());
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
        assertNull(result.getInstanceName());
        assertNull(result.getTimezone());
        verify(settingsRepository).getSettings();
    }

    @Test
    void testHandle_WithSettingsHavingNullCredential_ShouldReturnNullCredentialId() {
        // Arrange
        Settings settings = new Settings();
        settings.setId(1L);
        settings.setGithubCredentialId(null);
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

    @Test
    void testHandle_DefaultValueHelpers_ShouldReturnDefaults() {
        // Arrange
        when(settingsRepository.getSettings()).thenReturn(Optional.empty());

        // Act
        SettingsDto result = handler.handle(new GetSettingsQuery());

        // Assert - Test default value helpers
        assertEquals("Knull CI", result.getInstanceNameOrDefault());
        assertEquals("UTC", result.getTimezoneOrDefault());
        assertEquals(5, result.getMaxConcurrentBuildsOrDefault());
        assertEquals(60, result.getBuildTimeoutMinutesOrDefault());
        assertEquals(30, result.getBuildRetentionDaysOrDefault());
        assertTrue(result.getAutoCleanupWorkspaceOrDefault());
    }
}
