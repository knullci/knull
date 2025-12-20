package org.knullci.knull.persistence.mapper;

import org.knullci.knull.domain.model.Settings;

public class SettingsMapper {

    public static org.knullci.knull.persistence.entity.Settings toEntity(Settings settings) {
        if (settings == null) {
            return null;
        }
        return new org.knullci.knull.persistence.entity.Settings(
                settings.getId(),
                settings.getGithubCredentialId()
        );
    }

    public static Settings fromEntity(org.knullci.knull.persistence.entity.Settings settingsEntity) {
        if (settingsEntity == null) {
            return null;
        }
        return new Settings(
                settingsEntity.getId(),
                settingsEntity.getGithubCredentialId()
        );
    }
}
