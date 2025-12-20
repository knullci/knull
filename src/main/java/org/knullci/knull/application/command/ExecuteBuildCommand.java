package org.knullci.knull.application.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.knullci.knull.domain.model.Job;

@Getter
@AllArgsConstructor
public class ExecuteBuildCommand {
    
    private Job job;
    
    private String commitSha;
    
    private String commitMessage;
    
    private String branch;
    
    private String repositoryOwner;
    
    private String repositoryName;
    
    private String repositoryUrl;
    
    private String triggeredBy;
    
}
