package org.knullci.knull.persistence.repository;

import org.knullci.knull.domain.model.Settings;
import org.knullci.knull.domain.repository.SettingsRepository;
import org.knullci.knull.persistence.mapper.SettingsMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class SettingsRepositoryImpl implements SettingsRepository {

    private static final Logger logger = LoggerFactory.getLogger(SettingsRepositoryImpl.class);
    
    private final KnullRepository<org.knullci.knull.persistence.entity.Settings> knullRepository;
    private static final String SETTINGS_STORAGE_LOCATION = "storage/settings";
    private static final String SETTINGS_FILE_NAME = "settings.json";

    public SettingsRepositoryImpl() {
        this.knullRepository = new JsonKnullRepository<>(
                SETTINGS_STORAGE_LOCATION,
                org.knullci.knull.persistence.entity.Settings.class
        );
    }

    @Override
    public Settings saveSettings(Settings settings) {
        var _settings = SettingsMapper.toEntity(settings);
        _settings.setId(1L);
        this.knullRepository.save(SETTINGS_FILE_NAME, _settings);
        logger.info("Saved settings");
        return SettingsMapper.fromEntity(_settings);
    }

    @Override
    public Optional<Settings> getSettings() {
        logger.info("Fetching settings");
        return Optional.ofNullable(this.knullRepository.getByFileName(SETTINGS_FILE_NAME))
                .map(SettingsMapper::fromEntity);
    }
}
