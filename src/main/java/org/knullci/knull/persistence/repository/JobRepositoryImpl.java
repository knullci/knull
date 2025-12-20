package org.knullci.knull.persistence.repository;

import org.knullci.knull.domain.model.Job;
import org.knullci.knull.domain.repository.JobRepository;
import org.knullci.knull.persistence.mapper.JobMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class JobRepositoryImpl implements JobRepository {

    private final static Logger logger = LoggerFactory.getLogger(JobRepositoryImpl.class);

    private final KnullRepository<org.knullci.knull.persistence.entity.Job> knullRepository;

    private final static String JOB_STORAGE_LOCATION = "storage/jobs";

    public JobRepositoryImpl() {
        this.knullRepository = new JsonKnullRepository<>(
                JOB_STORAGE_LOCATION,
                org.knullci.knull.persistence.entity.Job.class
        );
    }

    @Override
    public List<Job> getAllJobs() {
        logger.info("Fetching all jobs");
        return this.knullRepository.getAll()
                .stream()
                .map(JobMapper::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Job> getJobId(Long jobId) {
        logger.info("Fetching job by id: {}", jobId);
        return Optional.ofNullable(this.knullRepository.getByFileName(jobId.toString() + ".json"))
                .map(JobMapper::fromEntity);
    }

    @Override
    public Optional<Job> getJobByRepoName(String repoName) {
        logger.info("Fetching job for repoName: {}", repoName);
        return this.knullRepository.getAll()
                .stream()
                .filter(job -> job.getJobConfig().getGitRepository().equalsIgnoreCase(repoName))
                .findFirst()
                .map(JobMapper::fromEntity);
    }

    @Override
    public void saveJob(Job job) {
        var _job = JobMapper.toEntity(job);
        _job.setId(this.knullRepository.getNextFileId());
        this.knullRepository.save(_job.getId().toString(), _job);
        logger.info("Saved new job");
    }

    @Override
    public void updateJob(Job job) {
        this.knullRepository.save(job.getId().toString(), JobMapper.toEntity(job));
        logger.info("Updated job with id: {}", job.getId());
    }

    @Override
    public void deleteJobById(Long jobId) {
        this.knullRepository.deleteByFileName(jobId.toString());
        logger.info("Deleted job with id: {}", jobId);
    }
}
