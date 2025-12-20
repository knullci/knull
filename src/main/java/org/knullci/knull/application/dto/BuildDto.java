package org.knullci.knull.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.knullci.knull.domain.enums.BuildStatus;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BuildDto {
    
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
    
    private Date startedAt;
    
    private Date completedAt;
    
    private Long duration;
    
    private String triggeredBy;
    
}
