package org.knullci.knull.persistence.mapper;

import org.knullci.knull.domain.model.BuildStep;

public class BuildStepMapper {

    public static org.knullci.knull.persistence.entity.BuildStep toEntity(BuildStep step) {
        if (step == null) {
            return null;
        }
        return new org.knullci.knull.persistence.entity.BuildStep(
                step.getName(),
                step.getStatus(),
                step.getOutput(),
                step.getStartedAt(),
                step.getCompletedAt(),
                step.getDuration(),
                step.getErrorMessage()
        );
    }

    public static BuildStep fromEntity(org.knullci.knull.persistence.entity.BuildStep stepEntity) {
        if (stepEntity == null) {
            return null;
        }
        return new BuildStep(
                stepEntity.getName(),
                stepEntity.getStatus(),
                stepEntity.getOutput(),
                stepEntity.getStartedAt(),
                stepEntity.getCompletedAt(),
                stepEntity.getDuration(),
                stepEntity.getErrorMessage()
        );
    }
}
