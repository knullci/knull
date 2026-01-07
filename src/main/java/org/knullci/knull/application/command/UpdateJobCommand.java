package org.knullci.knull.application.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.knullci.knull.domain.enums.JobType;

@Getter
@AllArgsConstructor
public class UpdateJobCommand {

    private Long id;

    private String name;

    private String description;

    private JobType jobType;

    private boolean cleanupWorkspace;

    private boolean checkoutLatestCommit;

    private String gitRepository;

    private Long credentialId;

    private String branch;

    private String branchPattern;

    private String scriptFileLocation;
}
