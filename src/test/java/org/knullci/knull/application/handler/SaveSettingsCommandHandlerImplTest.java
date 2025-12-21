package org.knullci.knull.application.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.knullci.knull.application.command.SaveSettingsCommand;
import org.knullci.knull.domain.model.Settings;
import org.knullci.knull.domain.repository.SettingsRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaveSettingsCommandHandlerImplTest {

    @Mock
    private SettingsRepository settingsRepository;

    @InjectMocks
    private SaveSettingsCommandHandlerImpl handler;

    @Test
    void testHandle_WithValidCommand_ShouldSaveSettings() {
        // Arrange
        Long credentialId = 123L;
        SaveSettingsCommand command = new SaveSettingsCommand(credentialId);
        when(settingsRepository.saveSettings(any(Settings.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        handler.handle(command);

        // Assert
        verify(settingsRepository).saveSettings(any(Settings.class));
    }

    @Test
    void testHandle_ShouldSetCorrectValues() {
        // Arrange
        Long credentialId = 456L;
        SaveSettingsCommand command = new SaveSettingsCommand(credentialId);

        ArgumentCaptor<Settings> settingsCaptor = ArgumentCaptor.forClass(Settings.class);
        when(settingsRepository.saveSettings(settingsCaptor.capture())).thenAnswer(i -> i.getArgument(0));

        // Act
        handler.handle(command);

        // Assert
        Settings capturedSettings = settingsCaptor.getValue();
        assertNotNull(capturedSettings);
        assertEquals(1L, capturedSettings.getId());
        assertEquals(456L, capturedSettings.getGithubCredentialId());
        verify(settingsRepository).saveSettings(any(Settings.class));
    }

    @Test
    void testHandle_WithNullCredentialId_ShouldStillSave() {
        // Arrange
        SaveSettingsCommand command = new SaveSettingsCommand(null);

        ArgumentCaptor<Settings> settingsCaptor = ArgumentCaptor.forClass(Settings.class);
        when(settingsRepository.saveSettings(settingsCaptor.capture())).thenAnswer(i -> i.getArgument(0));

        // Act
        handler.handle(command);

        // Assert
        Settings capturedSettings = settingsCaptor.getValue();
        assertNotNull(capturedSettings);
        assertNull(capturedSettings.getGithubCredentialId());
        verify(settingsRepository).saveSettings(any(Settings.class));
    }

    @Test
    void testHandle_ShouldCallRepositoryOnce() {
        // Arrange
        SaveSettingsCommand command = new SaveSettingsCommand(789L);
        when(settingsRepository.saveSettings(any(Settings.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        handler.handle(command);

        // Assert
        verify(settingsRepository, times(1)).saveSettings(any(Settings.class));
    }
}
