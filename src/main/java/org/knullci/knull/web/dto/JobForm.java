package org.knullci.knull.web.dto;

import lombok.Data;
import org.knullci.knull.domain.enums.JobType;

@Data
public class JobForm {
    private Long id;

    private String name;

    private String description;
    private String data;
    private JobType jobType;
}
