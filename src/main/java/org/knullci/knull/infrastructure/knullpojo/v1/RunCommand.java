package org.knullci.knull.infrastructure.knullpojo.v1;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RunCommand {

    private String tool;
    private List<String> args;

}
