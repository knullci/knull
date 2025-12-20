package org.knullci.knull.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MultiBranchJobConfig extends JobConfig {
    private String branchPattern;

    private String scriptFileLocation;

    public MultiBranchJobConfig(Long id, String gitRepository, Credentials credentials, String branchPattern, String scriptFileLocation) {
        super(id, gitRepository, credentials);
        this.branchPattern = branchPattern;
        this.scriptFileLocation = scriptFileLocation;
    }
    
    @Override
    public String getBuildScript() {
        return scriptFileLocation;
    }
}
