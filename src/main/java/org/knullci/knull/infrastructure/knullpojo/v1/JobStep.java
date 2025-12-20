package org.knullci.knull.infrastructure.knullpojo.v1;

import lombok.Data;

@Data
public class JobStep {

    private String name;
    private RunCommand run;

}
