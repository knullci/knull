package org.knullci.knull.domain.repository;

import org.knullci.knull.domain.model.Job;

import java.util.List;
import java.util.Optional;

public interface JobRepository {
    List<Job> getAllJobs();
    Optional<Job> getJobId(Long jobId);
    void saveJob(Job job);
    void updateJob(Job job);
    void deleteJobById(Long jobId);
}
