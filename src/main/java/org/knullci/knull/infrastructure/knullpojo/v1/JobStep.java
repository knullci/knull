package org.knullci.knull.infrastructure.knullpojo.v1;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class JobStep {

    private String name;
    
    @JsonAlias("command")
    private RunCommand run;

}
