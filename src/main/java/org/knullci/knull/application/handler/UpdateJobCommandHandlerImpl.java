package org.knullci.knull.application.handler;

import org.knullci.knull.application.command.UpdateJobCommand;
import org.knullci.knull.application.interfaces.UpdateJobCommandHandler;
import org.knullci.knull.domain.enums.JobType;
import org.knullci.knull.domain.model.Credentials;
import org.knullci.knull.domain.model.Job;
import org.knullci.knull.domain.model.JobConfig;
import org.knullci.knull.domain.model.MultiBranchJobConfig;
import org.knullci.knull.domain.model.SimpleJobConfig;
import org.knullci.knull.domain.repository.CredentialRepository;
import org.knullci.knull.domain.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UpdateJobCommandHandlerImpl implements UpdateJobCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(UpdateJobCommandHandlerImpl.class);

    private final JobRepository jobRepository;
    private final CredentialRepository credentialRepository;

    public UpdateJobCommandHandlerImpl(JobRepository jobRepository, CredentialRepository credentialRepository) {
        this.jobRepository = jobRepository;
        this.credentialRepository = credentialRepository;
    }

    @Override
    public void handle(UpdateJobCommand command) {
        logger.info("Updating job with id: {}", command.getId());

        // Fetch the existing job
        var existingJob = jobRepository.getJobId(command.getId())
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + command.getId()));

        // Fetch credentials if provided
        Credentials credentials = null;
        if (command.getCredentialId() != null) {
            credentials = credentialRepository.findById(command.getCredentialId())
                    .orElseThrow(
                            () -> new RuntimeException("Credential not found with id: " + command.getCredentialId()));
        }

        // Build job config based on job type
        JobConfig jobConfig = null;
        if (command.getJobType() == JobType.SIMPLE) {
            jobConfig = new SimpleJobConfig(
                    existingJob.getJobConfig() != null ? existingJob.getJobConfig().getId() : null,
                    command.getGitRepository(),
                    credentials,
                    command.getBranch(),
                    command.getScriptFileLocation());
        } else if (command.getJobType() == JobType.MULTIBRANCH) {
            jobConfig = new MultiBranchJobConfig(
                    existingJob.getJobConfig() != null ? existingJob.getJobConfig().getId() : null,
                    command.getGitRepository(),
                    credentials,
                    command.getBranchPattern(),
                    command.getScriptFileLocation());
        }

        // Create updated job
        Job updatedJob = new Job(
                command.getId(),
                command.getName(),
                command.getDescription(),
                command.getJobType(),
                jobConfig,
                command.isCleanupWorkspace(),
                command.isCheckoutLatestCommit(),
                existingJob.getCreatedBy(),
                existingJob.getCreatedAt(),
                null, // modifiedBy - will be set later if needed
                new Date() // modifiedAt
        );

        jobRepository.updateJob(updatedJob);
        logger.info("Job updated successfully with id: {}", command.getId());
    }
}
