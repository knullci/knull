package org.knullci.knull.persistence.mapper;

import org.knullci.knull.persistence.entity.JobConfig;
import org.knullci.knull.persistence.entity.SimpleJobConfig;

public class JobConfigMapper {
    public static JobConfig toEntity(org.knullci.knull.domain.model.JobConfig jobConfig) {
        if (jobConfig == null) return null;

        if (jobConfig instanceof org.knullci.knull.domain.model.SimpleJobConfig simpleJobConfig) {
            return toEntity(simpleJobConfig);
        }

        throw new IllegalArgumentException("Unknown domain model: " + jobConfig.getClass());
    }

    public static org.knullci.knull.domain.model.JobConfig fromEntity(JobConfig jobConfig) {
        if (jobConfig == null) return null;

        if (jobConfig instanceof SimpleJobConfig simpleJobConfig) {
            return fromEntity(simpleJobConfig);
        }

        throw new IllegalArgumentException("Unknown entity: " + jobConfig.getClass());
    }

    public static SimpleJobConfig toEntity(org.knullci.knull.domain.model.SimpleJobConfig simpleJobConfig) {
        var _simpleJobConfig = new SimpleJobConfig(
                simpleJobConfig.getBranch(),
                simpleJobConfig.getScriptFileLocation()
        );
        _simpleJobConfig.setId(simpleJobConfig.getId());
        _simpleJobConfig.setGitRepository(simpleJobConfig.getGitRepository());
        _simpleJobConfig.setCredentials(CredentialsMapper.toEntity(simpleJobConfig.getCredentials()));

        return _simpleJobConfig;
    }

    public static org.knullci.knull.domain.model.SimpleJobConfig fromEntity(SimpleJobConfig simpleJobConfig) {
        return new org.knullci.knull.domain.model.SimpleJobConfig(
                simpleJobConfig.getId(),
                simpleJobConfig.getGitRepository(),
                CredentialsMapper.fromEntity(simpleJobConfig.getCredentials()),
                simpleJobConfig.getBranch(),
                simpleJobConfig.getScriptFileLocation()
        );
    }
}
