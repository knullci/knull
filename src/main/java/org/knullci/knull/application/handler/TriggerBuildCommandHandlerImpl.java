package org.knullci.knull.application.handler;

import org.knullci.knull.application.command.ExecuteBuildCommand;
import org.knullci.knull.application.command.TriggerBuildCommand;
import org.knullci.knull.application.interfaces.ExecuteBuildCommandHandler;
import org.knullci.knull.application.interfaces.TriggerBuildCommandHandler;
import org.knullci.knull.domain.enums.JobType;
import org.knullci.knull.domain.model.Build;
import org.knullci.knull.domain.model.Credentials;
import org.knullci.knull.domain.model.Job;
import org.knullci.knull.domain.model.SimpleJobConfig;
import org.knullci.knull.domain.repository.CredentialRepository;
import org.knullci.knull.domain.repository.JobRepository;
import org.knullci.knull.infrastructure.dto.GithubBranchInfoDto;
import org.knullci.knull.infrastructure.service.EncryptionService;
import org.knullci.knull.infrastructure.service.GithubService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Handler for triggering manual builds.
 * Fetches the latest commit from GitHub and starts a build.
 */
@Service
public class TriggerBuildCommandHandlerImpl implements TriggerBuildCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(TriggerBuildCommandHandlerImpl.class);

    private final JobRepository jobRepository;
    private final CredentialRepository credentialRepository;
    private final GithubService githubService;
    private final EncryptionService encryptionService;
    private final ExecuteBuildCommandHandler executeBuildCommandHandler;

    public TriggerBuildCommandHandlerImpl(
            JobRepository jobRepository,
            CredentialRepository credentialRepository,
            GithubService githubService,
            EncryptionService encryptionService,
            ExecuteBuildCommandHandler executeBuildCommandHandler) {
        this.jobRepository = jobRepository;
        this.credentialRepository = credentialRepository;
        this.githubService = githubService;
        this.encryptionService = encryptionService;
        this.executeBuildCommandHandler = executeBuildCommandHandler;
    }

    @Override
    public Build handle(TriggerBuildCommand command) {
        logger.info("Triggering manual build for job ID: {}", command.getJobId());

        // Fetch the job
        Optional<Job> jobOpt = jobRepository.getJobId(command.getJobId());
        if (jobOpt.isEmpty()) {
            throw new RuntimeException("Job not found with ID: " + command.getJobId());
        }

        Job job = jobOpt.get();

        // Currently only support SIMPLE job type for manual trigger
        if (job.getJobType() != JobType.SIMPLE) {
            throw new RuntimeException("Manual trigger is only supported for SIMPLE job type");
        }

        SimpleJobConfig config = (SimpleJobConfig) job.getJobConfig();
        String gitRepository = config.getGitRepository();
        String branch = config.getBranch();

        // Parse repository URL to get owner and repo name
        // Expected format: https://github.com/owner/repo or
        // https://github.com/owner/repo.git
        String[] repoParts = parseGitRepository(gitRepository);
        String owner = repoParts[0];
        String repoName = repoParts[1];

        // Get the credential and decrypt the token
        Credentials credentials = config.getCredentials();
        if (credentials == null || credentials.getTokenCredential() == null) {
            throw new RuntimeException("No token credential configured for this job");
        }

        // Fetch full credential details
        Optional<Credentials> fullCredential = credentialRepository.findById(credentials.getId());
        if (fullCredential.isEmpty() || fullCredential.get().getTokenCredential() == null) {
            throw new RuntimeException("Could not fetch credential details");
        }

        String decryptedToken;
        try {
            decryptedToken = encryptionService.decrypt(fullCredential.get().getTokenCredential().getEncryptedToken());
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt token: " + e.getMessage());
        }

        // Fetch latest commit from GitHub
        Optional<GithubBranchInfoDto> branchInfo = githubService.getLatestCommit(owner, repoName, branch,
                decryptedToken);
        if (branchInfo.isEmpty()) {
            throw new RuntimeException("Could not fetch latest commit for branch: " + branch);
        }

        GithubBranchInfoDto.CommitInfo commitInfo = branchInfo.get().getCommit();
        String commitSha = commitInfo.getSha();
        String commitMessage = commitInfo.getCommit() != null ? commitInfo.getCommit().getMessage() : "Manual trigger";

        // Truncate commit message if too long
        if (commitMessage != null && commitMessage.length() > 200) {
            commitMessage = commitMessage.substring(0, 200) + "...";
        }

        logger.info("Triggering build for job: {} with commit: {} on branch: {}",
                job.getName(), commitSha, branch);

        // Execute the build
        ExecuteBuildCommand buildCommand = new ExecuteBuildCommand(
                job,
                commitSha,
                commitMessage,
                branch,
                owner,
                repoName,
                gitRepository,
                command.getTriggeredBy());

        executeBuildCommandHandler.handle(buildCommand);

        logger.info("Manual build triggered successfully for job: {}", job.getName());
        return null; // Build is executed asynchronously
    }

    /**
     * Parses a Git repository URL to extract owner and repo name.
     * Supports formats like:
     * - https://github.com/owner/repo
     * - https://github.com/owner/repo.git
     * - git@github.com:owner/repo.git
     */
    private String[] parseGitRepository(String gitRepository) {
        String cleaned = gitRepository;

        // Remove .git suffix if present
        if (cleaned.endsWith(".git")) {
            cleaned = cleaned.substring(0, cleaned.length() - 4);
        }

        // Handle HTTPS URLs
        if (cleaned.contains("github.com/")) {
            String[] parts = cleaned.split("github.com/");
            if (parts.length > 1) {
                String[] ownerRepo = parts[1].split("/");
                if (ownerRepo.length >= 2) {
                    return new String[] { ownerRepo[0], ownerRepo[1] };
                }
            }
        }

        // Handle SSH URLs (git@github.com:owner/repo)
        if (cleaned.contains("github.com:")) {
            String[] parts = cleaned.split("github.com:");
            if (parts.length > 1) {
                String[] ownerRepo = parts[1].split("/");
                if (ownerRepo.length >= 2) {
                    return new String[] { ownerRepo[0], ownerRepo[1] };
                }
            }
        }

        throw new RuntimeException("Could not parse Git repository URL: " + gitRepository);
    }
}
