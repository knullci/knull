package org.knullci.knull.infrastructure.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.knullci.knull.domain.enums.BuildStepStatus;
import org.knullci.knull.domain.model.*;
import org.knullci.knull.domain.repository.BuildRepository;
import org.knullci.knull.domain.repository.CredentialRepository;
import org.knullci.knull.infrastructure.dto.ProcessResult;
import org.knullci.knull.infrastructure.knullpojo.v1.RunCommand;
import org.knullci.knull.proto.ExecuteRequest;
import org.knullci.knull.proto.ExecuteResponse;
import org.knullci.knull.proto.ExecuteStreamResponse;
import org.knullci.knull.proto.ExecutorServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class NecroswordExecutor implements KnullExecutor {

    private static final Logger logger = LoggerFactory.getLogger(NecroswordExecutor.class);
    private final CredentialRepository credentialRepository;
    private final BuildRepository buildRepository;
    private final EncryptionService encryptionService;
    private final ObjectMapper yamlObjectMapper;

    // Workspace configuration - shared path between Knull and Necrosword
    @Value("${knull.workspace.base-path:/tmp/knull-workspace}")
    private String workspaceBasePath;

    // gRPC configuration - configure in application.properties
    @Value("${necrosword.grpc.host:localhost}")
    private String grpcHost;

    @Value("${necrosword.grpc.port:8081}")
    private int grpcPort;

    private ManagedChannel channel;
    private ExecutorServiceGrpc.ExecutorServiceStub asyncStub;
    private ExecutorServiceGrpc.ExecutorServiceBlockingStub blockingStub;

    public NecroswordExecutor(CredentialRepository credentialRepository,
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
        logger.info("Initializing gRPC channel to Necrosword at {}:{}", grpcHost, grpcPort);
        this.channel = ManagedChannelBuilder.forAddress(grpcHost, grpcPort)
                .usePlaintext() // Use TLS in production!
                .build();
        this.asyncStub = ExecutorServiceGrpc.newStub(channel);
        this.blockingStub = ExecutorServiceGrpc.newBlockingStub(channel);
    }

    @PreDestroy
    public void shutdown() {
        if (channel != null) {
            try {
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.warn("Error shutting down gRPC channel", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void executeBuild(Build build, Job job) {
        logger.info("Starting build execution for build ID: {}", build.getId());

        String workspaceDir = workspaceBasePath + "/build-" + build.getId();
        JobConfig jobConfig = job.getJobConfig();

        try {
            // Step 1: Prepare workspace
            executeStep(build, "Prepare Workspace", () -> prepareWorkspace(workspaceDir));

            // Step 2: Clone repository
            executeStep(build, "Clone Repository",
                    () -> cloneRepository(build, jobConfig.getCredentials(), workspaceDir));

            // Step 3: Checkout branch
            executeStep(build, "Checkout Branch", () -> checkoutBranch(build, workspaceDir + "/" + build.getRepositoryName()));

            // Step 4: Checkout specific commit
            if (!job.isCheckoutLatestCommit()) {
                executeStep(build, "Checkout Commit", () -> checkoutCommit(build, workspaceDir + "/" + build.getRepositoryName()));
            }

            logger.info("Build execution completed successfully for build ID: {}", build.getId());

        } catch (Exception e) {
            logger.error("Build execution failed for build ID: {}", build.getId(), e);
            throw new RuntimeException("Build execution failed: " + e.getMessage(), e);
        } finally {
            // Cleanup workspace
            try {
                if (job.isCleanupWorkspace()) {
                     executeStep(build, "Cleanup Workspace", () -> cleanupWorkspace(workspaceDir));
                }
            } catch (Exception e) {
                logger.warn("Failed to cleanup workspace for build ID: {}", build.getId(), e);
            }
        }
    }

    private void executeStep(Build build, String stepName, StepExecutor executor) throws Exception {
        BuildStep step = new BuildStep();
        step.setName(stepName);
        step.setStatus(BuildStepStatus.IN_PROGRESS);
        step.setStartedAt(new Date());

        build.getSteps().add(step);
        logger.info("Executing step: {}", stepName);

        StringBuilder output = new StringBuilder();
        long startTime = System.currentTimeMillis();
        // Append step header to incremental build log and persist
        StringBuilder incLog = new StringBuilder(build.getBuildLog() == null ? "" : build.getBuildLog());
        incLog.append("\n=== ").append(stepName).append(" ===\n");
        build.setBuildLog(incLog.toString());
        buildRepository.updateBuild(build);

        try {
            String result = executor.execute();
            output.append(result);

            step.setStatus(BuildStepStatus.SUCCESS);
            step.setOutput(output.toString());
            logger.info("Step completed successfully: {}", stepName);
            // Append output to incremental log and persist
            incLog.append(output);
            build.setBuildLog(incLog.toString());
            buildRepository.updateBuild(build);

        } catch (Exception e) {
            step.setStatus(BuildStepStatus.FAILURE);
            step.setErrorMessage(e.getMessage());
            output.append("\nError: ").append(e.getMessage());
            step.setOutput(output.toString());
            logger.error("Step failed: {}", stepName, e);
            // Append error to incremental log and persist before rethrow
            incLog.append(output);
            build.setBuildLog(incLog.toString());
            buildRepository.updateBuild(build);
            throw e;
        } finally {
            step.setCompletedAt(new Date());
            step.setDuration(System.currentTimeMillis() - startTime);
            // Persist step timing and status changes
            buildRepository.updateBuild(build);
        }
    }

    private String prepareWorkspace(String workspaceDir) throws Exception {
        Path workspacePath = Paths.get(workspaceDir);

        // Clean up existing directory if it exists
        if (Files.exists(workspacePath)) {
            deleteDirectory(workspacePath.toFile());
        }

        // Create workspace directory
        Files.createDirectories(workspacePath);

        return "Workspace prepared at: " + workspacePath.toAbsolutePath();
    }

    private String cloneRepository(Build build, Credentials credentials, String workspaceDir) throws Exception {
        String authenticatedUrl = buildAuthenticatedUrl(build.getRepositoryUrl(), credentials);

        ExecuteRequest request = ExecuteRequest.newBuilder()
                .setTool("git")
                .addAllArgs(Arrays.asList("clone", authenticatedUrl))
                .setWorkDir(workspaceDir)
                .build();

        ExecuteResponse response = blockingStub.execute(request);

        if (!response.getSuccess()) {
            throw new RuntimeException("Git clone failed: " + response.getError());
        }

        return response.getStdout() + response.getStderr();
    }

    private String checkoutBranch(Build build, String workspaceDir) throws Exception {
        ExecuteRequest request = ExecuteRequest.newBuilder()
                .setTool("git")
                .addAllArgs(Arrays.asList("checkout", build.getBranch()))
                .setWorkDir(workspaceDir)
                .build();

        ExecuteResponse response = blockingStub.execute(request);

        if (!response.getSuccess()) {
            throw new RuntimeException("Git checkout branch failed: " + response.getError());
        }

        return response.getStdout() + response.getStderr();
    }

    private String checkoutCommit(Build build, String workspaceDir) throws Exception {
        ExecuteRequest request = ExecuteRequest.newBuilder()
                .setTool("git")
                .addAllArgs(Arrays.asList("checkout", build.getCommitSha()))
                .setWorkDir(workspaceDir)
                .build();

        ExecuteResponse response = blockingStub.execute(request);

        if (!response.getSuccess()) {
            throw new RuntimeException("Git checkout commit failed: " + response.getError());
        }

        return response.getStdout() + response.getStderr();
    }

    private String cleanupWorkspace(String workspaceDir) throws Exception {
        Path workspacePath = Paths.get(workspaceDir);

        if (Files.exists(workspacePath)) {
            deleteDirectory(workspacePath.toFile());
            return "Workspace cleaned up: " + workspacePath.toAbsolutePath();
        }

        return "Workspace already clean";
    }

    private String buildAuthenticatedUrl(String repositoryUrl, Credentials credentials) throws Exception {
        if (credentials == null) {
            throw new RuntimeException("No credentials configured for repository clone");
        }

        // Fetch the full credential details from repository
        var credentialOpt = credentialRepository.findById(credentials.getId());
        if (credentialOpt.isEmpty()) {
            throw new RuntimeException("Credential not found: " + credentials.getId());
        }

        var credential = credentialOpt.get();

        // Extract domain from URL (e.g., https://github.com/user/repo.git ->
        // github.com)
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
            // For GitHub token: https://TOKEN@github.com/user/repo.git
            return protocol + token + "@" + url;
        } else if (credential.getUsernamePasswordCredential() != null) {
            String username = credential.getUsernamePasswordCredential().getUsername();
            String password = encryptionService
                    .decrypt(credential.getUsernamePasswordCredential().getEncryptedPassword());
            // For username/password: https://username:password@github.com/user/repo.git
            return protocol + username + ":" + password + "@" + url;
        }

        throw new RuntimeException("Credential does not contain token or username/password");
    }

    private void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }

    @FunctionalInterface
    private interface StepExecutor {
        String execute() throws Exception;
    }
}
