package org.knullci.knull.domain.repository;

import org.knullci.knull.domain.model.Settings;

import java.util.Optional;

public interface SettingsRepository {
    
    Settings saveSettings(Settings settings);
    
    Optional<Settings> getSettings();
    
}
