package org.knullci.knull.application.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SaveSettingsCommand {

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
     * Constructor for GitHub settings only (backward compatibility)
     */
    public SaveSettingsCommand(Long githubCredentialId) {
        this.githubCredentialId = githubCredentialId;
        this.instanceName = null;
        this.timezone = null;
        this.maxConcurrentBuilds = null;
        this.buildTimeoutMinutes = null;
        this.buildRetentionDays = null;
        this.autoCleanupWorkspace = null;
    }
}
