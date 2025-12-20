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
        logger.info("Saving settings with GitHub credential ID: {}", command.getGithubCredentialId());
        
        Settings settings = new Settings();
        settings.setId(1L);
        settings.setGithubCredentialId(command.getGithubCredentialId());
        
        this.settingsRepository.saveSettings(settings);
    }
}
