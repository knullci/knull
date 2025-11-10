package org.knullci.knull.persistence.mapper;

import org.knullci.knull.persistence.entity.Job;
import org.knullci.knull.persistence.enums.JobType;

public class JobMapper {
    public static Job toEntity(org.knullci.knull.domain.model.Job job) {
        var _job = new Job();
        _job.setId(job.getId());
        _job.setName(job.getName());
        _job.setDescription(job.getDescription());
        _job.setJobType(JobType.valueOf(job.getJobType().name()));
        _job.setJobConfig(JobConfigMapper.toEntity(job.getJobConfig()));
        _job.setCreateAt(job.getCreateAt());
        _job.setCreatedBy(UserMapper.toEntity(job.getCreatedBy()));
        _job.setModifiedAt(job.getModifiedAt());
        _job.setModifiedBy(UserMapper.toEntity(job.getModifiedBy()));

        return _job;
    }

    public static org.knullci.knull.domain.model.Job fromEntity(Job job) {
        return new org.knullci.knull.domain.model.Job(
                job.getId(),
                job.getName(),
                job.getDescription(),
                org.knullci.knull.domain.enums.JobType.valueOf(job.getJobType().name()),
                JobConfigMapper.fromEntity(job.getJobConfig()),
                UserMapper.fromEntity(job.getCreatedBy()),
                job.getCreateAt(),
                UserMapper.fromEntity(job.getModifiedBy()),
                job.getModifiedAt()
        );
    }
}
