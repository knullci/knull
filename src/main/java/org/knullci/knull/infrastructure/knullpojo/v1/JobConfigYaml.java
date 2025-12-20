package org.knullci.knull.infrastructure.knullpojo.v1;

import lombok.Data;

import java.util.List;

@Data
public class JobConfigYaml {

    private String name;
    private List<JobStep> steps;

}
