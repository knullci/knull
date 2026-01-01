package org.knullci.knull.application.handler;

import org.knullci.knull.application.command.ExecuteBuildCommand;
import org.knullci.knull.application.constant.KnullConstant;
import org.knullci.knull.application.interfaces.ExecuteBuildCommandHandler;
import org.knullci.knull.domain.enums.BuildStatus;
import org.knullci.knull.domain.model.Build;
import org.knullci.knull.domain.repository.BuildRepository;
import org.knullci.knull.infrastructure.dto.UpdateCommitStatusDto;
import org.knullci.knull.infrastructure.enums.GHCommitState;
import org.knullci.knull.infrastructure.service.BuildExecutorService;
import org.knullci.knull.infrastructure.service.GithubService;
import org.knullci.knull.infrastructure.service.KnullExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class ExecuteBuildCommandHandlerImpl implements ExecuteBuildCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(ExecuteBuildCommandHandlerImpl.class);

    private final BuildRepository buildRepository;
    private final GithubService githubService;
    private final BuildExecutorService buildExecutorService;
    private final KnullExecutor knullExecutor;

    public ExecuteBuildCommandHandlerImpl(BuildRepository buildRepository,
            GithubService githubService,
            BuildExecutorService buildExecutorService, KnullExecutor knullExecutor) {
        this.buildRepository = buildRepository;
        this.githubService = githubService;
        this.buildExecutorService = buildExecutorService;
        this.knullExecutor = knullExecutor;
    }

    @Override
    @Async
    public void handle(ExecuteBuildCommand command) {
        logger.info("Starting build execution for job: {}, commit: {}",
                command.getJob().getName(), command.getCommitSha());

        // Create build record
        Build build = new Build();
        build.setJobId(command.getJob().getId());
        build.setJobName(command.getJob().getName());
        build.setCommitSha(command.getCommitSha());
        build.setCommitMessage(command.getCommitMessage());
        build.setBranch(command.getBranch());
        build.setRepositoryUrl(command.getRepositoryUrl());
        build.setRepositoryOwner(command.getRepositoryOwner());
        build.setRepositoryName(command.getRepositoryName());
        build.setStatus(BuildStatus.IN_PROGRESS);
        build.setStartedAt(new Date());
        build.setTriggeredBy(command.getTriggeredBy());
        build.setBuildLog("Build started...\n");

        // Save initial build
        build = buildRepository.saveBuild(build);

        // Update GitHub status to IN_PROGRESS
        githubService.updateCommitStatus(new UpdateCommitStatusDto(
                command.getRepositoryOwner(),
                command.getRepositoryName(),
                command.getCommitSha(),
                GHCommitState.PENDING,
                "http://localhost:8080/builds/" + build.getId() + "/pipeline",
                "Build #" + build.getId() + " is in progress...",
                KnullConstant.BUILD_CONTEXT));

        try {
            // Execute the build using BuildExecutorService
            logger.info("Executing build for job: {}", command.getJob().getName());
            // buildExecutorService.executeBuild(build, command.getJob());
            knullExecutor.executeBuild(build, command.getJob());

            // Consolidate build logs from steps
            StringBuilder consolidatedLog = new StringBuilder(build.getBuildLog());
            build.getSteps().forEach(step -> {
                consolidatedLog.append("\n=== ").append(step.getName()).append(" ===\n");
                consolidatedLog.append("Status: ").append(step.getStatus()).append("\n");
                if (step.getOutput() != null) {
                    consolidatedLog.append(step.getOutput()).append("\n");
                }
                if (step.getErrorMessage() != null) {
                    consolidatedLog.append("Error: ").append(step.getErrorMessage()).append("\n");
                }
            });

            // Update build status to SUCCESS
            build.setStatus(BuildStatus.SUCCESS);
            build.setCompletedAt(new Date());
            build.setDuration(build.getCompletedAt().getTime() - build.getStartedAt().getTime());
            build.setBuildLog(consolidatedLog.toString() + "\nBuild completed successfully!");
            buildRepository.updateBuild(build);

            // Update GitHub status to SUCCESS
            githubService.updateCommitStatus(new UpdateCommitStatusDto(
                    command.getRepositoryOwner(),
                    command.getRepositoryName(),
                    command.getCommitSha(),
                    GHCommitState.SUCCESS,
                    "http://localhost:8080/builds/" + build.getId() + "/pipeline",
                    "Build #" + build.getId() + " passed",
                    KnullConstant.BUILD_CONTEXT));

            logger.info("Build {} completed successfully", build.getId());

        } catch (Exception e) {
            logger.error("Build {} failed: {}", build.getId(), e.getMessage(), e);

            // Check if the build was cancelled - if so, don't overwrite the status
            Build currentBuild = buildRepository.findById(build.getId()).orElse(build);
            boolean wasCancelled = currentBuild.getStatus() == BuildStatus.CANCELLED;

            // Consolidate build logs from steps even on failure
            StringBuilder consolidatedLog = new StringBuilder(currentBuild.getBuildLog());
            currentBuild.getSteps().forEach(step -> {
                consolidatedLog.append("\n=== ").append(step.getName()).append(" ===\n");
                consolidatedLog.append("Status: ").append(step.getStatus()).append("\n");
                if (step.getOutput() != null) {
                    consolidatedLog.append(step.getOutput()).append("\n");
                }
                if (step.getErrorMessage() != null) {
                    consolidatedLog.append("Error: ").append(step.getErrorMessage()).append("\n");
                }
            });

            // Only update status if not already cancelled
            if (!wasCancelled) {
                currentBuild.setStatus(BuildStatus.FAILURE);
                currentBuild.setCompletedAt(new Date());
                currentBuild
                        .setDuration(currentBuild.getCompletedAt().getTime() - currentBuild.getStartedAt().getTime());
                currentBuild.setBuildLog(consolidatedLog.toString() + "\nBuild failed: " + e.getMessage());
                buildRepository.updateBuild(currentBuild);

                // Update GitHub status to FAILURE
                githubService.updateCommitStatus(new UpdateCommitStatusDto(
                        command.getRepositoryOwner(),
                        command.getRepositoryName(),
                        command.getCommitSha(),
                        GHCommitState.FAILURE,
                        "http://localhost:8080/builds/" + currentBuild.getId() + "/pipeline",
                        "Build #" + currentBuild.getId() + " failed",
                        KnullConstant.BUILD_CONTEXT));
            } else {
                logger.info("Build {} was cancelled, not overwriting status to FAILURE", currentBuild.getId());
            }
        }
    }
}
