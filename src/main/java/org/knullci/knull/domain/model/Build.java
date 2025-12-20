package org.knullci.knull.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.knullci.knull.domain.enums.BuildStatus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Build {
    
    private Long id;
    
    private Long jobId;
    
    private String jobName;
    
    private String commitSha;
    
    private String commitMessage;
    
    private String branch;
    
    private String repositoryUrl;
    
    private String repositoryOwner;
    
    private String repositoryName;
    
    private BuildStatus status;
    
    private String buildLog;
    
    private List<BuildStep> steps = new ArrayList<>();
    
    private Date startedAt;
    
    private Date completedAt;
    
    private Long duration; // in milliseconds
    
    private String triggeredBy;
    
}
