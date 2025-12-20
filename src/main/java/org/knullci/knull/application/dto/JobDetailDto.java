package org.knullci.knull.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.knullci.knull.domain.enums.JobType;

import java.util.Date;

@AllArgsConstructor
@Getter
public class JobDetailDto {
    private Long id;
    private String name;
    private String description;
    private JobType jobType;
    
    // Job Config details
    private String gitRepository;
    private String credentialName;
    private String branch;
    private String branchPattern;
    private String scriptFileLocation;
    
    private Date createdAt;
}
