package org.knullci.knull.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.knullci.knull.domain.enums.JobType;

import java.util.Date;

@Getter
@AllArgsConstructor
public class Job {
    private Long id;

    private String name;

    private String description;

    private JobType jobType;

    private JobConfig jobConfig;

    private boolean cleanupWorkspace;

    private boolean checkoutLatestCommit;

    private User createdBy;

    private Date createdAt;

    private User modifiedBy;

    private Date modifiedAt;
}
