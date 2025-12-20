package org.knullci.knull.application.handler;

import org.knullci.knull.application.dto.SettingsDto;
import org.knullci.knull.application.interfaces.GetSettingsQueryHandler;
import org.knullci.knull.application.query.GetSettingsQuery;
import org.knullci.knull.domain.repository.SettingsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GetSettingsQueryHandlerImpl implements GetSettingsQueryHandler {

    private static final Logger logger = LoggerFactory.getLogger(GetSettingsQueryHandlerImpl.class);

    private final SettingsRepository settingsRepository;

    public GetSettingsQueryHandlerImpl(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    @Override
    public SettingsDto handle(GetSettingsQuery query) {
        logger.info("Fetching settings");
        
        return this.settingsRepository.getSettings()
                .map(settings -> new SettingsDto(
                        settings.getId(),
                        settings.getGithubCredentialId()
                ))
                .orElse(new SettingsDto(1L, null));
    }
}
