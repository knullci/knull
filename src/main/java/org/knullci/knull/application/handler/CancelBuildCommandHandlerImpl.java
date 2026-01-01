package org.knullci.knull.application.handler;

import org.knullci.knull.application.command.CancelBuildCommand;
import org.knullci.knull.application.dto.CancelBuildResult;
import org.knullci.knull.application.interfaces.CancelBuildCommandHandler;
import org.knullci.knull.domain.enums.BuildStatus;
import org.knullci.knull.domain.enums.BuildStepStatus;
import org.knullci.knull.domain.model.Build;
import org.knullci.knull.domain.repository.BuildRepository;
import org.knullci.knull.infrastructure.service.NecroswordExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

/**
 * Handler for cancelling running builds.
 * This moves the core business logic from BuildController to the application
 * layer.
 */
@Service
public class CancelBuildCommandHandlerImpl implements CancelBuildCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(CancelBuildCommandHandlerImpl.class);

    private final BuildRepository buildRepository;
    private final NecroswordExecutor necroswordExecutor;

    public CancelBuildCommandHandlerImpl(BuildRepository buildRepository,
            NecroswordExecutor necroswordExecutor) {
        this.buildRepository = buildRepository;
        this.necroswordExecutor = necroswordExecutor;
    }

    @Override
    public CancelBuildResult handle(CancelBuildCommand command) {
        Long buildId = command.getBuildId();
        logger.info("Processing cancel build request for build ID: {}", buildId);

        // Find the build
        Optional<Build> buildOpt = buildRepository.findById(buildId);
        if (buildOpt.isEmpty()) {
            logger.warn("Build not found for cancellation: {}", buildId);
            return CancelBuildResult.failure("Build not found");
        }

        Build build = buildOpt.get();

        // Check if build is still running
        if (build.getStatus() != BuildStatus.IN_PROGRESS) {
            logger.warn("Cannot cancel build {} - status is {}", buildId, build.getStatus());
            return CancelBuildResult.failure("Build is not in progress. Current status: " + build.getStatus().name());
        }

        // Signal cancellation to the executor
        boolean cancelled = necroswordExecutor.cancelBuild(buildId);

        if (cancelled) {
            // Update build status
            build.setStatus(BuildStatus.CANCELLED);
            build.setCompletedAt(new Date());
            if (build.getStartedAt() != null) {
                build.setDuration(build.getCompletedAt().getTime() - build.getStartedAt().getTime());
            }

            // Mark any IN_PROGRESS steps as cancelled
            build.getSteps().stream()
                    .filter(step -> step.getStatus() == BuildStepStatus.IN_PROGRESS)
                    .forEach(step -> {
                        step.setStatus(BuildStepStatus.FAILURE);
                        step.setErrorMessage("Build cancelled by user");
                        step.setCompletedAt(new Date());
                    });

            // Append cancellation log
            build.setBuildLog(build.getBuildLog() + "\n\n=== BUILD CANCELLED ===\nBuild was cancelled by user.");

            // Persist changes
            buildRepository.updateBuild(build);

            logger.info("Build {} cancelled successfully", buildId);
            return CancelBuildResult.success("Build cancelled successfully");
        } else {
            logger.warn("Failed to cancel build {} - not found in running builds", buildId);
            return CancelBuildResult.failure("Build not found in running builds (may have already completed)");
        }
    }
}
