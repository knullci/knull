package org.knullci.knull.infrastructure.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.knullci.knull.domain.enums.BuildStepStatus;
import org.knullci.knull.domain.model.Build;
import org.knullci.knull.domain.model.BuildStep;
import org.knullci.knull.domain.model.Credentials;
import org.knullci.knull.domain.model.JobConfig;
import org.knullci.knull.domain.repository.BuildRepository;
import org.knullci.knull.domain.repository.CredentialRepository;
import org.knullci.knull.infrastructure.dto.ProcessResult;
import org.knullci.knull.infrastructure.enums.Tool;
import org.knullci.knull.infrastructure.knullpojo.v1.JobConfigYaml;
import org.knullci.knull.infrastructure.knullpojo.v1.JobStep;
import org.knullci.knull.infrastructure.knullpojo.v1.RunCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class BuildExecutorService {

    private static final Logger logger = LoggerFactory.getLogger(BuildExecutorService.class);
    private static final String WORKSPACE_BASE = "workspace";
    private final KnullProcessRunner processRunner;
    private final CredentialRepository credentialRepository;
    private final BuildRepository buildRepository;
    private final EncryptionService encryptionService;

    public BuildExecutorService(KnullProcessRunner processRunner, 
                               CredentialRepository credentialRepository,
                               EncryptionService encryptionService,
                               BuildRepository buildRepository) {
        this.processRunner = processRunner;
        this.credentialRepository = credentialRepository;
        this.encryptionService = encryptionService;
        this.buildRepository = buildRepository;
    }

    public void executeBuild(Build build, JobConfig jobConfig) {
        logger.info("Starting build execution for build ID: {}", build.getId());
        
        String workspaceDir = WORKSPACE_BASE + "/build-" + build.getId();
        
        try {
            // Step 1: Prepare workspace
            executeStep(build, "Prepare Workspace", () -> prepareWorkspace(workspaceDir));
            
            // Step 2: Clone repository
            executeStep(build, "Clone Repository", () -> cloneRepository(build, jobConfig.getCredentials(), workspaceDir));
            
            // Step 3: Checkout branch
            executeStep(build, "Checkout Branch", () -> checkoutBranch(build, workspaceDir));
            
            // Step 4: Checkout specific commit
            executeStep(build, "Checkout Commit", () -> checkoutCommit(build, workspaceDir));
            
            // Step 5: Execute build script
            executeStep(build, "Execute Build Script", () -> executeBuildScript(build, jobConfig, workspaceDir));
            
            logger.info("Build execution completed successfully for build ID: {}", build.getId());
            
        } catch (Exception e) {
            logger.error("Build execution failed for build ID: {}", build.getId(), e);
            throw new RuntimeException("Build execution failed: " + e.getMessage(), e);
        } finally {
            // Cleanup workspace
            try {
                executeStep(build, "Cleanup Workspace", () -> cleanupWorkspace(workspaceDir));
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
        
        RunCommand command = new RunCommand(
            "git",
            Arrays.asList("clone", authenticatedUrl, workspaceDir)
        );
        
        ProcessResult result = processRunner.run(command);
        
        if (!result.success()) {
            throw new RuntimeException("Git clone failed: " + result.error());
        }
        
        return result.output() + result.error();
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
        
        // Extract domain from URL (e.g., https://github.com/user/repo.git -> github.com)
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
            String password = encryptionService.decrypt(credential.getUsernamePasswordCredential().getEncryptedPassword());
            // For username/password: https://username:password@github.com/user/repo.git
            return protocol + username +":" + password + "@" + url;
        }
        
        throw new RuntimeException("Credential does not contain token or username/password");
    }

    private String checkoutBranch(Build build, String workspaceDir) throws Exception {
        RunCommand command = new RunCommand(
            "git",
            Arrays.asList("checkout", build.getBranch())
        );
        
        ProcessResult result = processRunner.run(command, Paths.get(workspaceDir));
        
        if (!result.success()) {
            throw new RuntimeException("Git checkout branch failed: " + result.error());
        }
        
        return result.output() + result.error();
    }

    private String checkoutCommit(Build build, String workspaceDir) throws Exception {
        RunCommand command = new RunCommand(
            "git",
            Arrays.asList("checkout", build.getCommitSha())
        );
        
        ProcessResult result = processRunner.run(command, Paths.get(workspaceDir));
        
        if (!result.success()) {
            throw new RuntimeException("Git checkout commit failed: " + result.error());
        }
        
        return result.output() + result.error();
    }

    private String executeBuildScript(Build build, JobConfig jobConfig, String workspaceDir) throws Exception {
        String scriptFileLocation = jobConfig.getBuildScript();
        
        if (scriptFileLocation == null || scriptFileLocation.trim().isEmpty()) {
            return "No build script file configured, skipping build execution";
        }
        
        // Read the YAML script file from workspace
        Path scriptPath = Paths.get(workspaceDir, scriptFileLocation);
        if (!Files.exists(scriptPath)) {
            throw new RuntimeException("Build script file not found: " + scriptFileLocation);
        }
        
        logger.info("Parsing YAML build script from: {}", scriptPath);
        
        // Parse YAML file
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        JobConfigYaml jobConfigYaml = yamlMapper.readValue(scriptPath.toFile(), JobConfigYaml.class);
        
        if (jobConfigYaml.getSteps() == null || jobConfigYaml.getSteps().isEmpty()) {
            return "No steps defined in build script";
        }
        
        StringBuilder output = new StringBuilder();
        output.append("Executing job: ").append(jobConfigYaml.getName()).append("\n");
        output.append("Total steps: ").append(jobConfigYaml.getSteps().size()).append("\n\n");
        
        // Execute each step in the YAML file
        for (int i = 0; i < jobConfigYaml.getSteps().size(); i++) {
            JobStep jobStep = jobConfigYaml.getSteps().get(i);
            
            output.append("Step ").append(i + 1).append(": ").append(jobStep.getName()).append("\n");
            logger.info("Executing step: {}", jobStep.getName());
            
            RunCommand runCommand = jobStep.getRun();
            if (runCommand == null) {
                output.append("  Skipped (no run command defined)\n\n");
                continue;
            }
            
            // Validate tool is in allowed list
            try {
                Tool.from(runCommand.getTool());
            } catch (SecurityException e) {
                throw new RuntimeException("Step '" + jobStep.getName() + "' uses disallowed tool: " + 
                    runCommand.getTool() + ". Allowed tools: git, npm, mvn, docker, kubectl");
            }
            
            // Execute command in workspace directory
            ProcessResult result = processRunner.run(runCommand, Paths.get(workspaceDir));
            
            output.append("  Tool: ").append(runCommand.getTool()).append("\n");
            output.append("  Args: ").append(runCommand.getArgs()).append("\n");
            output.append("  Output:\n");
            output.append(result.output());
            if (!result.error().isEmpty()) {
                output.append(result.error());
            }
            output.append("\n");
            
            if (!result.success()) {
                throw new RuntimeException("Step '" + jobStep.getName() + "' failed with exit code: " + result.exitCode());
            }
        }
        
        output.append("\nAll steps completed successfully!");
        return output.toString();
    }

    private String cleanupWorkspace(String workspaceDir) throws Exception {
        Path workspacePath = Paths.get(workspaceDir);
        
        if (Files.exists(workspacePath)) {
            deleteDirectory(workspacePath.toFile());
            return "Workspace cleaned up: " + workspacePath.toAbsolutePath();
        }
        
        return "Workspace already clean";
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
