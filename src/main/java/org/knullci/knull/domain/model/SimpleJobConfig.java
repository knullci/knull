package org.knullci.knull.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SimpleJobConfig extends JobConfig {
    private String branch;

    private String scriptFileLocation;

    public SimpleJobConfig(Long id, String gitRepository, Credentials credentials, String branch, String scriptFileLocation) {
        super(id, gitRepository, credentials);
        this.branch = branch;
        this.scriptFileLocation = scriptFileLocation;
    }
}
