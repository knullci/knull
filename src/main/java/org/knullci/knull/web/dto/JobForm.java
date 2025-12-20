package org.knullci.knull.web.dto;

import lombok.Data;
import org.knullci.knull.domain.enums.JobType;

@Data
public class JobForm {
    private String name;
    private String description;
    private JobType jobType;
    
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
