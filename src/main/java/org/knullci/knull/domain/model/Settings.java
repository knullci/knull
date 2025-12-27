package org.knullci.knull.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Settings {

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

}
