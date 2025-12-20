package org.knullci.knull.persistence.mapper;

import org.knullci.knull.domain.model.Build;

import java.util.stream.Collectors;

public class BuildMapper {

    public static org.knullci.knull.persistence.entity.Build toEntity(Build build) {
        if (build == null) {
            return null;
        }
        return new org.knullci.knull.persistence.entity.Build(
                build.getId(),
                build.getJobId(),
                build.getJobName(),
                build.getCommitSha(),
                build.getCommitMessage(),
                build.getBranch(),
                build.getRepositoryUrl(),
                build.getRepositoryOwner(),
                build.getRepositoryName(),
                build.getStatus(),
                build.getBuildLog(),
                build.getSteps() != null ? build.getSteps().stream()
                        .map(BuildStepMapper::toEntity)
                        .collect(Collectors.toList()) : null,
                build.getStartedAt(),
                build.getCompletedAt(),
                build.getDuration(),
                build.getTriggeredBy()
        );
    }

    public static Build fromEntity(org.knullci.knull.persistence.entity.Build buildEntity) {
        if (buildEntity == null) {
            return null;
        }
        return new Build(
                buildEntity.getId(),
                buildEntity.getJobId(),
                buildEntity.getJobName(),
                buildEntity.getCommitSha(),
                buildEntity.getCommitMessage(),
                buildEntity.getBranch(),
                buildEntity.getRepositoryUrl(),
                buildEntity.getRepositoryOwner(),
                buildEntity.getRepositoryName(),
                buildEntity.getStatus(),
                buildEntity.getBuildLog(),
                buildEntity.getSteps() != null ? buildEntity.getSteps().stream()
                        .map(BuildStepMapper::fromEntity)
                        .collect(Collectors.toList()) : null,
                buildEntity.getStartedAt(),
                buildEntity.getCompletedAt(),
                buildEntity.getDuration(),
                buildEntity.getTriggeredBy()
        );
    }
}
