package org.knullci.knull.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.knullci.knull.domain.enums.BuildStepStatus;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BuildStep {
    
    private String name;
    
    private BuildStepStatus status;
    
    private String output;
    
    private Date startedAt;
    
    private Date completedAt;
    
    private Long duration;
    
    private String errorMessage;
    
}
