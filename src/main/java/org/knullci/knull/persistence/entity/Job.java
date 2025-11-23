package org.knullci.knull.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.knullci.knull.persistence.enums.JobType;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Job {

    private Long id;

    private String name;

    private String description;

    private JobType jobType;

    private JobConfig jobConfig;

    private User createdBy;

    private Date createAt;

    private User modifiedBy;

    private Date modifiedAt;

}
