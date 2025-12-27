package org.knullci.knull.application.handler;

import org.knullci.knull.application.command.SaveSettingsCommand;
import org.knullci.knull.application.interfaces.SaveSettingsCommandHandler;
import org.knullci.knull.domain.model.Settings;
import org.knullci.knull.domain.repository.SettingsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SaveSettingsCommandHandlerImpl implements SaveSettingsCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(SaveSettingsCommandHandlerImpl.class);

    private final SettingsRepository settingsRepository;

    public SaveSettingsCommandHandlerImpl(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    @Override
    public void handle(SaveSettingsCommand command) {
        logger.info("Saving settings");

        // Get existing settings or create new ones
        Settings settings = settingsRepository.getSettings()
                .orElseGet(() -> {
                    Settings newSettings = new Settings();
                    newSettings.setId(1L);
                    return newSettings;
                });

        // Update GitHub credential if provided (or explicitly set to null)
        if (command.getGithubCredentialId() != null ||
                (command.getInstanceName() == null && command.getTimezone() == null)) {
            // This is a GitHub settings update
            settings.setGithubCredentialId(command.getGithubCredentialId());
        }

        // Update general settings if provided
        if (command.getInstanceName() != null) {
            settings.setInstanceName(command.getInstanceName());
        }
        if (command.getTimezone() != null) {
            settings.setTimezone(command.getTimezone());
        }
        if (command.getMaxConcurrentBuilds() != null) {
            settings.setMaxConcurrentBuilds(command.getMaxConcurrentBuilds());
        }
        if (command.getBuildTimeoutMinutes() != null) {
            settings.setBuildTimeoutMinutes(command.getBuildTimeoutMinutes());
        }
        if (command.getBuildRetentionDays() != null) {
            settings.setBuildRetentionDays(command.getBuildRetentionDays());
        }
        if (command.getAutoCleanupWorkspace() != null) {
            settings.setAutoCleanupWorkspace(command.getAutoCleanupWorkspace());
        }

        this.settingsRepository.saveSettings(settings);
        logger.info("Settings saved successfully");
    }
}
