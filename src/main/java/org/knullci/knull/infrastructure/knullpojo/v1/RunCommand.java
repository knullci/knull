package org.knullci.knull.infrastructure.knullpojo.v1;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class RunCommand {

    private String tool;
    private List<String> args;
    
    @JsonCreator
    public RunCommand(@JsonProperty("tool") String tool, @JsonProperty("args") List<String> args) {
        this.tool = tool;
        this.args = args;
    }

}
