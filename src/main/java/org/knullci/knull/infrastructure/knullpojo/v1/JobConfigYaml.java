package org.knullci.knull.infrastructure.knullpojo.v1;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.util.List;

@Data
public class JobConfigYaml {

    private String name;
    
    @JsonAlias("stages")
    private List<JobStep> steps;

}
