package org.knullci.knull.infrastructure.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.knullci.knull.domain.enums.BuildStepStatus;
import org.knullci.knull.domain.model.Build;
import org.knullci.knull.domain.model.Credentials;
import org.knullci.knull.domain.model.Job;
import org.knullci.knull.domain.repository.BuildRepository;
import org.knullci.knull.domain.repository.CredentialRepository;
import org.knullci.knull.infrastructure.knullpojo.v1.JobConfigYaml;
import org.knullci.knull.infrastructure.knullpojo.v1.JobStep;
import org.knullci.knull.infrastructure.knullpojo.v1.JobYaml;
import org.knullci.knull.proto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.File;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * NecroswordExecutor - Executes builds using the Necrosword gRPC pipeline
 * service.
 * This executor leverages the gRPC streaming pipeline API for:
 * - Real-time log streaming
 * - Efficient multistep execution
 * - Better error handling and timeout management
 */
@Service
public class NecroswordExecutor implements KnullExecutor {

    private static final Logger logger = LoggerFactory.getLogger(NecroswordExecutor.class);

    private static final int DEFAULT_STEP_TIMEOUT_SECONDS = 300; // 5 minutes per step
    private static final int DEFAULT_PIPELINE_TIMEOUT_SECONDS = 3600; // 1 hour total
    private static final int CHANNEL_SHUTDOWN_TIMEOUT_SECONDS = 5;

    private final CredentialRepository credentialRepository;
    private final BuildRepository buildRepository;
    private final EncryptionService encryptionService;
    private final ObjectMapper yamlObjectMapper;

    @Value("${knull.workspace.base-path:/tmp/knull-workspace}")
    private String workspaceBasePath;

    @Value("${necrosword.grpc.host:localhost}")
    private String grpcHost;

    @Value("${necrosword.grpc.port:8081}")
    private int grpcPort;

    @Value("${necrosword.grpc.use-tls:false}")
    private boolean useTls;

    @Value("${necrosword.grpc.max-inbound-message-size:16777216}")
    private int maxInboundMessageSize; // 16MB default

    private ManagedChannel channel;
    private ExecutorServiceGrpc.ExecutorServiceStub asyncStub;
    private ExecutorServiceGrpc.ExecutorServiceBlockingStub blockingStub;

    public NecroswordExecutor(
            CredentialRepository credentialRepository,
            EncryptionService encryptionService,
            BuildRepository buildRepository,
            @Qualifier("yamlObjectMapper") ObjectMapper yamlObjectMapper) {
        this.credentialRepository = credentialRepository;
        this.encryptionService = encryptionService;
        this.buildRepository = buildRepository;
        this.yamlObjectMapper = yamlObjectMapper;
    }

    @PostConstruct
    public void init() {
        initializeGrpcChannel();
    }

    private void initializeGrpcChannel() {
        logger.info("Initializing gRPC channel to Necrosword at {}:{}", grpcHost, grpcPort);

        ManagedChannelBuilder<?> channelBuilder = ManagedChannelBuilder
                .forAddress(grpcHost, grpcPort)
                .maxInboundMessageSize(maxInboundMessageSize)
                .keepAliveTime(30, TimeUnit.SECONDS)
                .keepAliveTimeout(10, TimeUnit.SECONDS)
                .keepAliveWithoutCalls(true);

        if (!useTls) {
            channelBuilder.usePlaintext();
            logger.warn("gRPC channel is using plaintext. Enable TLS in production!");
        }

        this.channel = channelBuilder.build();
        this.asyncStub = ExecutorServiceGrpc.newStub(channel);
        this.blockingStub = ExecutorServiceGrpc.newBlockingStub(channel);

        logger.info("gRPC channel initialized successfully");
    }

    @PreDestroy
    public void shutdown() {
        shutdownGrpcChannel();
    }

    private void shutdownGrpcChannel() {
        if (channel != null && !channel.isShutdown()) {
            try {
                logger.info("Shutting down gRPC channel...");
                channel.shutdown();
                if (!channel.awaitTermination(CHANNEL_SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    logger.warn("Channel did not terminate in time, forcing shutdown");
                    channel.shutdownNow();
                }
                logger.info("gRPC channel shutdown complete");
            } catch (InterruptedException e) {
                logger.warn("Interrupted while shutting down gRPC channel", e);
                channel.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void executeBuild(Build build, Job job) {
        logger.info("Starting build execution for build ID: {} using Necrosword gRPC pipeline", build.getId());

        String workspaceDir = workspaceBasePath + "/build-" + build.getId();
        String repoDir = workspaceDir + "/" + build.getRepositoryName();

        try {
            // Phase 1: Prepare workspace and clone repository (setup steps)
            executeSetupPhase(build, job, workspaceDir, repoDir);

            // Phase 2: Execute the build pipeline from job configuration
            executeBuildPipeline(build, job, repoDir);

            logger.info("Build execution completed successfully for build ID: {}", build.getId());

        } catch (Exception e) {
            logger.error("Build execution failed for build ID: {}", build.getId(), e);
            throw new RuntimeException("Build execution failed: " + e.getMessage(), e);
        } finally {
            // Cleanup workspace if configured
            cleanupIfRequired(build, job, workspaceDir);
        }
    }

    /**
     * Execute the setup phase: workspace preparation and repository checkout.
     * Note: We DON'T set workspaceDir on the pipeline because we're creating it in
     * the first step.
     */
    private void executeSetupPhase(Build build, Job job, String workspaceDir, String repoDir) throws Exception {
        logger.info("Executing setup phase for build ID: {}", build.getId());

        // Build setup pipeline steps
        List<org.knullci.knull.proto.BuildStep> setupSteps = new ArrayList<>();

        // Step 1: Create workspace directory (using mkdir) - NO workDir since we're
        // creating it
        setupSteps.add(org.knullci.knull.proto.BuildStep.newBuilder()
                .setName("Create Workspace")
                .setTool("mkdir")
                .addAllArgs(Arrays.asList("-p", workspaceDir))
                .setTimeoutSeconds(30)
                .build());

        // Step 2: Clone repository - NOW we can use workspaceDir as workDir
        String authenticatedUrl = buildAuthenticatedUrl(build.getRepositoryUrl(), job.getJobConfig().getCredentials());
        setupSteps.add(org.knullci.knull.proto.BuildStep.newBuilder()
                .setName("Clone Repository")
                .setTool("git")
                .addAllArgs(Arrays.asList("clone", authenticatedUrl, build.getRepositoryName()))
                .setWorkDir(workspaceDir)
                .setTimeoutSeconds(300)
                .build());

        // Step 3: Checkout branch
        setupSteps.add(org.knullci.knull.proto.BuildStep.newBuilder()
                .setName("Checkout Branch")
                .setTool("git")
                .addAllArgs(Arrays.asList("checkout", build.getBranch()))
                .setWorkDir(repoDir)
                .setTimeoutSeconds(60)
                .build());

        // Step 4: Checkout specific commit (if not latest)
        if (!job.isCheckoutLatestCommit() && build.getCommitSha() != null && !build.getCommitSha().isEmpty()) {
            setupSteps.add(org.knullci.knull.proto.BuildStep.newBuilder()
                    .setName("Checkout Commit")
                    .setTool("git")
                    .addAllArgs(Arrays.asList("checkout", build.getCommitSha()))
                    .setWorkDir(repoDir)
                    .setTimeoutSeconds(60)
                    .build());
        }

        // Create setup pipeline - NOTE: No workspaceDir set here, each step specifies
        // its own
        PipelineRequest setupPipeline = PipelineRequest.newBuilder()
                .setId("setup-" + build.getId())
                .setName("Setup Pipeline")
                // DON'T set workspaceDir here - first step creates it!
                .addAllSteps(setupSteps)
                .setTimeoutSeconds(600) // 10 minutes for setup
                .build();

        executePipelineWithStreaming(build, setupPipeline, "Setup");
    }

    /**
     * Execute the main build pipeline from job configuration.
     */
    private void executeBuildPipeline(Build build, Job job, String repoDir) throws Exception {
        logger.info("Executing build pipeline for build ID: {}", build.getId());

        // Load the job YAML configuration
        String buildScript = job.getJobConfig().getBuildScript();
        if (buildScript == null || buildScript.isEmpty()) {
            logger.info("No build script configured for job, skipping build pipeline");
            return;
        }

        File scriptFile = new File(repoDir, buildScript);
        if (!scriptFile.exists()) {
            throw new RuntimeException("Build script not found: " + scriptFile.getAbsolutePath());
        }

        JobYaml jobYaml = yamlObjectMapper.readValue(scriptFile, JobYaml.class);

        // Use helper methods that support both flat and nested YAML structures
        List<JobStep> effectiveSteps = jobYaml.getEffectiveSteps();
        String effectiveName = jobYaml.getEffectiveName();

        if (effectiveSteps == null || effectiveSteps.isEmpty()) {
            logger.info("No steps defined in job configuration, skipping build pipeline");
            return;
        }

        logger.info("Found {} steps in build script: {}", effectiveSteps.size(), effectiveName);

        // Convert JobSteps to gRPC BuildSteps
        List<org.knullci.knull.proto.BuildStep> buildSteps = new ArrayList<>();
        for (JobStep step : effectiveSteps) {
            org.knullci.knull.proto.BuildStep.Builder stepBuilder = org.knullci.knull.proto.BuildStep.newBuilder()
                    .setName(step.getName() != null ? step.getName() : "Step " + (buildSteps.size() + 1))
                    .setTool(step.getRun().getTool())
                    .addAllArgs(step.getRun().getArgs() != null ? step.getRun().getArgs() : Collections.emptyList())
                    .setWorkDir(repoDir)
                    .setTimeoutSeconds(DEFAULT_STEP_TIMEOUT_SECONDS);

            buildSteps.add(stepBuilder.build());
        }

        // Create and execute the build pipeline
        // Note: Don't set workspaceDir here since each step already has workDir set
        PipelineRequest buildPipeline = PipelineRequest.newBuilder()
                .setId("build-" + build.getId())
                .setName(effectiveName != null ? effectiveName : "Build Pipeline")
                .addAllSteps(buildSteps)
                .setTimeoutSeconds(DEFAULT_PIPELINE_TIMEOUT_SECONDS)
                .build();

        executePipelineWithStreaming(build, buildPipeline, "Build");
    }

    /**
     * Execute a pipeline using gRPC streaming for real-time log updates.
     */
    private void executePipelineWithStreaming(Build build, PipelineRequest pipelineRequest, String phaseName)
            throws Exception {
        logger.info("Executing {} pipeline with {} steps for build ID: {}",
                phaseName, pipelineRequest.getStepsCount(), build.getId());

        CountDownLatch completionLatch = new CountDownLatch(1);
        AtomicBoolean success = new AtomicBoolean(true);
        AtomicReference<String> errorMessage = new AtomicReference<>();
        AtomicReference<PipelineResponse> pipelineResult = new AtomicReference<>();

        // Use streaming for real-time updates
        StreamObserver<PipelineStreamResponse> responseObserver = new StreamObserver<>() {
            private final StringBuilder currentStepOutput = new StringBuilder();
            private String currentStepName = null;
            private org.knullci.knull.domain.model.BuildStep currentBuildStep = null;
            private long stepStartTime = 0;

            @Override
            public void onNext(PipelineStreamResponse response) {
                try {
                    if (response.hasStepStarted()) {
                        handleStepStarted(response.getStepStarted());
                    } else if (response.hasStepOutput()) {
                        handleStepOutput(response.getStepOutput());
                    } else if (response.hasStepCompleted()) {
                        handleStepCompleted(response.getStepCompleted());
                    } else if (response.hasPipelineCompleted()) {
                        handlePipelineCompleted(response.getPipelineCompleted());
                    }
                } catch (Exception e) {
                    logger.error("Error processing pipeline stream response", e);
                }
            }

            private void handleStepStarted(StepStartedEvent event) {
                logger.info("[{}] Step started: {} ({}/{})",
                        phaseName, event.getStepName(), event.getStepIndex() + 1, event.getTotalSteps());

                currentStepName = event.getStepName();
                currentStepOutput.setLength(0);
                stepStartTime = System.currentTimeMillis();

                // Create and track build step
                currentBuildStep = new org.knullci.knull.domain.model.BuildStep();
                currentBuildStep.setName(event.getStepName());
                currentBuildStep.setStatus(BuildStepStatus.IN_PROGRESS);
                currentBuildStep.setStartedAt(new Date());
                build.getSteps().add(currentBuildStep);

                // Update build log with step header
                appendToBuildLog(build, "\n=== " + event.getStepName() + " ===\n");
            }

            private void handleStepOutput(StepOutputEvent event) {
                String line = event.hasStdoutLine() ? event.getStdoutLine() : event.getStderrLine();
                currentStepOutput.append(line).append("\n");

                // Real-time log update
                appendToBuildLog(build, line + "\n");
            }

            private void handleStepCompleted(StepResult result) {
                ExecuteResponse execResult = result.getExecuteResult();
                boolean stepSuccess = execResult != null && execResult.getSuccess();

                logger.info("[{}] Step completed: {} - Success: {}, ExitCode: {}",
                        phaseName, result.getName(), stepSuccess,
                        execResult != null ? execResult.getExitCode() : "N/A");

                // Log error details if step failed
                if (!stepSuccess && execResult != null) {
                    logger.error("[{}] Step '{}' failed - Error: {}, Stderr: {}",
                            phaseName, result.getName(),
                            execResult.getError(),
                            execResult.getStderr());
                }

                if (currentBuildStep != null) {
                    if (execResult != null) {
                        currentBuildStep.setStatus(execResult.getSuccess()
                                ? BuildStepStatus.SUCCESS
                                : BuildStepStatus.FAILURE);
                        currentBuildStep.setOutput(currentStepOutput.toString());
                        if (!execResult.getSuccess()) {
                            currentBuildStep.setErrorMessage(execResult.getError());
                        }
                    }
                    currentBuildStep.setCompletedAt(new Date());
                    currentBuildStep.setDuration(System.currentTimeMillis() - stepStartTime);
                }

                // Persist step completion
                buildRepository.updateBuild(build);
            }

            private void handlePipelineCompleted(PipelineResponse response) {
                logger.info("[{}] Pipeline completed - Success: {}, Duration: {}ms",
                        phaseName, response.getSuccess(), response.getTotalDurationMs());

                pipelineResult.set(response);
                success.set(response.getSuccess());
                if (!response.getSuccess()) {
                    // Build detailed error message including step result details
                    StringBuilder errMsg = new StringBuilder();
                    errMsg.append("Pipeline failed at step: ").append(response.getFailedStep());

                    // Find the failed step result and include error details
                    for (StepResult stepResult : response.getStepResultsList()) {
                        if (stepResult.getName().equals(response.getFailedStep())
                                && stepResult.getExecuteResult() != null) {
                            ExecuteResponse execResult = stepResult.getExecuteResult();
                            if (!execResult.getError().isEmpty()) {
                                errMsg.append(" - Error: ").append(execResult.getError());
                            }
                            if (!execResult.getStderr().isEmpty()) {
                                errMsg.append(" - Stderr: ").append(execResult.getStderr().trim());
                            }
                            break;
                        }
                    }
                    errorMessage.set(errMsg.toString());
                }
            }

            @Override
            public void onError(Throwable t) {
                logger.error("[{}] Pipeline execution error", phaseName, t);
                success.set(false);
                if (t instanceof StatusRuntimeException) {
                    errorMessage.set("gRPC error: " + ((StatusRuntimeException) t).getStatus().getDescription());
                } else {
                    errorMessage.set("Pipeline execution error: " + t.getMessage());
                }
                completionLatch.countDown();
            }

            @Override
            public void onCompleted() {
                logger.info("[{}] Pipeline stream completed", phaseName);
                completionLatch.countDown();
            }
        };

        // Execute the pipeline with streaming
        asyncStub.executePipelineStream(pipelineRequest, responseObserver);

        // Wait for completion with timeout
        boolean completed = completionLatch.await(DEFAULT_PIPELINE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (!completed) {
            throw new RuntimeException(
                    phaseName + " pipeline timed out after " + DEFAULT_PIPELINE_TIMEOUT_SECONDS + " seconds");
        }

        if (!success.get()) {
            throw new RuntimeException(
                    errorMessage.get() != null ? errorMessage.get() : phaseName + " pipeline failed");
        }

        logger.info("{} pipeline completed successfully for build ID: {}", phaseName, build.getId());
    }

    private void appendToBuildLog(Build build, String text) {
        String currentLog = build.getBuildLog() != null ? build.getBuildLog() : "";
        build.setBuildLog(currentLog + text);
        buildRepository.updateBuild(build);
    }

    private void cleanupIfRequired(Build build, Job job, String workspaceDir) {
        if (!job.isCleanupWorkspace()) {
            logger.info("Workspace cleanup disabled for build ID: {}", build.getId());
            return;
        }

        try {
            logger.info("Cleaning up workspace for build ID: {}", build.getId());

            java.nio.file.Path workspacePath = java.nio.file.Paths.get(workspaceDir);
            if (java.nio.file.Files.exists(workspacePath)) {
                // Delete directory recursively using Java NIO
                java.nio.file.Files.walk(workspacePath)
                        .sorted(java.util.Comparator.reverseOrder())
                        .map(java.nio.file.Path::toFile)
                        .forEach(java.io.File::delete);

                logger.info("Workspace cleanup completed for build ID: {}", build.getId());

                // Add cleanup step to build
                org.knullci.knull.domain.model.BuildStep cleanupStep = new org.knullci.knull.domain.model.BuildStep();
                cleanupStep.setName("Cleanup Workspace");
                cleanupStep.setStatus(BuildStepStatus.SUCCESS);
                cleanupStep.setOutput("Workspace cleaned up: " + workspaceDir);
                cleanupStep.setStartedAt(new Date());
                cleanupStep.setCompletedAt(new Date());
                build.getSteps().add(cleanupStep);
            } else {
                logger.info("Workspace already clean for build ID: {}", build.getId());
            }
        } catch (Exception e) {
            logger.warn("Failed to cleanup workspace for build ID: {}", build.getId(), e);
        }
    }

    private String buildAuthenticatedUrl(String repositoryUrl, Credentials credentials) throws Exception {
        if (credentials == null) {
            throw new RuntimeException("No credentials configured for repository clone");
        }

        Optional<Credentials> credentialOpt = credentialRepository.findById(credentials.getId());
        if (credentialOpt.isEmpty()) {
            throw new RuntimeException("Credential not found: " + credentials.getId());
        }

        Credentials credential = credentialOpt.get();

        // Parse URL
        String url = repositoryUrl;
        String protocol = "https://";
        if (url.startsWith("https://")) {
            url = url.substring(8);
        } else if (url.startsWith("http://")) {
            protocol = "http://";
            url = url.substring(7);
        }

        // Build authenticated URL based on credential type
        if (credential.getTokenCredential() != null) {
            String token = encryptionService.decrypt(credential.getTokenCredential().getEncryptedToken());
            return protocol + token + "@" + url;
        } else if (credential.getUsernamePasswordCredential() != null) {
            String username = credential.getUsernamePasswordCredential().getUsername();
            String password = encryptionService
                    .decrypt(credential.getUsernamePasswordCredential().getEncryptedPassword());
            return protocol + username + ":" + password + "@" + url;
        }

        throw new RuntimeException("Credential does not contain token or username/password");
    }

    public boolean isHealthy() {
        try {
            HealthResponse response = blockingStub
                    .withDeadlineAfter(5, TimeUnit.SECONDS)
                    .health(HealthRequest.newBuilder().build());
            return "healthy".equals(response.getStatus());
        } catch (Exception e) {
            logger.warn("Health check failed", e);
            return false;
        }
    }

    public int getRunningProcessCount() {
        try {
            GetProcessesResponse response = blockingStub
                    .withDeadlineAfter(5, TimeUnit.SECONDS)
                    .getRunningProcesses(GetProcessesRequest.newBuilder().build());
            return response.getCount();
        } catch (Exception e) {
            logger.warn("Failed to get running process count", e);
            return -1;
        }
    }
}
