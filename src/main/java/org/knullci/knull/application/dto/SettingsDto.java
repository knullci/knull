package org.knullci.knull.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SettingsDto {

    private Long id;

    // GitHub Integration
    private Long githubCredentialId;

    // General Settings
    private String instanceName;
    private String timezone;

    // Build Settings
    private Integer maxConcurrentBuilds;
    private Integer buildTimeoutMinutes;
    private Integer buildRetentionDays;
    private Boolean autoCleanupWorkspace;

    /**
     * Get instance name with default fallback
     */
    public String getInstanceNameOrDefault() {
        return instanceName != null && !instanceName.isEmpty() ? instanceName : "Knull CI";
    }

    /**
     * Get timezone with default fallback
     */
    public String getTimezoneOrDefault() {
        return timezone != null && !timezone.isEmpty() ? timezone : "UTC";
    }

    /**
     * Get max concurrent builds with default fallback
     */
    public Integer getMaxConcurrentBuildsOrDefault() {
        return maxConcurrentBuilds != null ? maxConcurrentBuilds : 5;
    }

    /**
     * Get build timeout with default fallback
     */
    public Integer getBuildTimeoutMinutesOrDefault() {
        return buildTimeoutMinutes != null ? buildTimeoutMinutes : 60;
    }

    /**
     * Get build retention days with default fallback
     */
    public Integer getBuildRetentionDaysOrDefault() {
        return buildRetentionDays != null ? buildRetentionDays : 30;
    }

    /**
     * Get auto cleanup workspace with default fallback
     */
    public Boolean getAutoCleanupWorkspaceOrDefault() {
        return autoCleanupWorkspace != null ? autoCleanupWorkspace : true;
    }
}
