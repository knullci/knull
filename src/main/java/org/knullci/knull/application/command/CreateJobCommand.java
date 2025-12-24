package org.knullci.knull.application.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.knullci.knull.domain.enums.JobType;

@Getter
@AllArgsConstructor
public class CreateJobCommand {
    private String name;
    private String description;
    private JobType jobType;
    private boolean cleanupWorkspace;
    private boolean checkoutLatestCommit;

    // Job Config fields
    private String gitRepository;
    private Long credentialId;

    // Simple Job Config fields
    private String branch;

    // Multi Branch Job Config fields
    private String branchPattern;

    // Common field for both types
    private String scriptFileLocation;
}
