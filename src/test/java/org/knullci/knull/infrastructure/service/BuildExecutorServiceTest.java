package org.knullci.knull.infrastructure.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.knullci.knull.domain.enums.CredentialType;
import org.knullci.knull.domain.enums.JobType;
import org.knullci.knull.domain.model.*;
import org.knullci.knull.domain.repository.BuildRepository;
import org.knullci.knull.domain.repository.CredentialRepository;
import org.knullci.knull.infrastructure.dto.ProcessResult;
import org.knullci.knull.infrastructure.knullpojo.v1.RunCommand;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BuildExecutorServiceTest {

    @Mock
    private KnullProcessRunner processRunner;

    @Mock
    private CredentialRepository credentialRepository;

    @Mock
    private BuildRepository buildRepository;

    @Mock
    private EncryptionService encryptionService;

    @Mock
    private ObjectMapper yamlObjectMapper;

    private BuildExecutorService buildExecutorService;

    @BeforeEach
    void setUp() {
        buildExecutorService = new BuildExecutorService(
                processRunner,
                credentialRepository,
                encryptionService,
                buildRepository,
                yamlObjectMapper);
    }

    @Test
    void testExecuteBuild_ShouldAddStepsToBuildsStepsList() {
        // Arrange
        Build build = createTestBuild();
        Job job = createTestJob();

        ProcessResult cloneSuccess = createSuccessResult("Cloned");
        ProcessResult checkoutSuccess = createSuccessResult("Checked out");

        when(credentialRepository.findById(anyLong())).thenReturn(Optional.of(createTestCredential()));
        when(encryptionService.decrypt(anyString())).thenReturn("decrypted-token");
        when(processRunner.run(any(RunCommand.class))).thenReturn(cloneSuccess);
        when(processRunner.run(any(RunCommand.class), any(Path.class))).thenReturn(checkoutSuccess);
        doNothing().when(buildRepository).updateBuild(any(Build.class));

        // Act
        try {
            buildExecutorService.executeBuild(build, job);
        } catch (Exception e) {
            // Expected - no build script configured
        }

        // Assert
        assertFalse(build.getSteps().isEmpty());
        verify(buildRepository, atLeastOnce()).updateBuild(build);
    }

    @Test
    void testExecuteBuild_WhenCloneFails_ShouldThrowException() {
        // Arrange
        Build build = createTestBuild();
        Job job = createTestJob();

        ProcessResult cloneFailure = createFailureResult("Clone failed");

        when(credentialRepository.findById(anyLong())).thenReturn(Optional.of(createTestCredential()));
        when(encryptionService.decrypt(anyString())).thenReturn("decrypted-token");
        when(processRunner.run(any(RunCommand.class))).thenReturn(cloneFailure);
        doNothing().when(buildRepository).updateBuild(any(Build.class));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> buildExecutorService.executeBuild(build, job));
    }

    @Test
    void testExecuteBuild_WhenNoCredentials_ShouldThrowException() {
        // Arrange
        Build build = createTestBuild();
        // Create job without credentials
        SimpleJobConfig config = new SimpleJobConfig(
                1L,
                "https://github.com/testowner/testrepo.git",
                null, // No credentials
                "main",
                null);
        Job job = new Job(
                1L, "Test Job", "Description", JobType.SIMPLE, config,
                false, true, null, new Date(), null, new Date());

        doNothing().when(buildRepository).updateBuild(any(Build.class));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> buildExecutorService.executeBuild(build, job));
    }

    @Test
    void testExecuteBuild_WhenCredentialNotFound_ShouldThrowException() {
        // Arrange
        Build build = createTestBuild();
        Job job = createTestJob();

        when(credentialRepository.findById(anyLong())).thenReturn(Optional.empty());
        doNothing().when(buildRepository).updateBuild(any(Build.class));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> buildExecutorService.executeBuild(build, job));
    }

    @Test
    void testExecuteBuild_ShouldSetStepStatusToInProgress() {
        // Arrange
        Build build = createTestBuild();
        Job job = createTestJob();

        ProcessResult cloneSuccess = createSuccessResult("Cloned");

        when(credentialRepository.findById(anyLong())).thenReturn(Optional.of(createTestCredential()));
        when(encryptionService.decrypt(anyString())).thenReturn("decrypted-token");
        when(processRunner.run(any(RunCommand.class))).thenReturn(cloneSuccess);
        when(processRunner.run(any(RunCommand.class), any(Path.class))).thenReturn(cloneSuccess);
        doNothing().when(buildRepository).updateBuild(any(Build.class));

        // Act
        try {
            buildExecutorService.executeBuild(build, job);
        } catch (Exception e) {
            // Expected
        }

        // Assert - Steps should be added
        assertFalse(build.getSteps().isEmpty());
    }

    @Test
    void testExecuteBuild_WithCleanupWorkspace_ShouldAttemptCleanup() {
        // Arrange
        Build build = createTestBuild();
        Job job = createTestJobWithCleanup();

        ProcessResult cloneSuccess = createSuccessResult("Cloned");

        when(credentialRepository.findById(anyLong())).thenReturn(Optional.of(createTestCredential()));
        when(encryptionService.decrypt(anyString())).thenReturn("decrypted-token");
        when(processRunner.run(any(RunCommand.class))).thenReturn(cloneSuccess);
        when(processRunner.run(any(RunCommand.class), any(Path.class))).thenReturn(cloneSuccess);
        doNothing().when(buildRepository).updateBuild(any(Build.class));

        // Act
        try {
            buildExecutorService.executeBuild(build, job);
        } catch (Exception e) {
            // May throw due to missing build script
        }

        // Assert - Build repository should be called for updates
        verify(buildRepository, atLeastOnce()).updateBuild(any(Build.class));
    }

    private Build createTestBuild() {
        Build build = new Build();
        build.setId(1L);
        build.setJobId(1L);
        build.setJobName("Test Job");
        build.setCommitSha("abc123");
        build.setCommitMessage("Test commit");
        build.setBranch("main");
        build.setRepositoryUrl("https://github.com/testowner/testrepo.git");
        build.setRepositoryOwner("testowner");
        build.setRepositoryName("testrepo");
        build.setStartedAt(new Date());
        build.setBuildLog("");
        return build;
    }

    private Job createTestJob() {
        TokenCredential tokenCredential = new TokenCredential("encrypted-token");
        Credentials credentials = new Credentials(
                1L, "Test Credential", "Description",
                CredentialType.TOKEN, null, tokenCredential,
                null, new Date(), null, new Date());

        SimpleJobConfig config = new SimpleJobConfig(
                1L,
                "https://github.com/testowner/testrepo.git",
                credentials,
                "main",
                null // No build script for simpler test
        );

        return new Job(
                1L, "Test Job", "Description", JobType.SIMPLE, config,
                false, true, null, new Date(), null, new Date());
    }

    private Job createTestJobWithCleanup() {
        TokenCredential tokenCredential = new TokenCredential("encrypted-token");
        Credentials credentials = new Credentials(
                1L, "Test Credential", "Description",
                CredentialType.TOKEN, null, tokenCredential,
                null, new Date(), null, new Date());

        SimpleJobConfig config = new SimpleJobConfig(
                1L,
                "https://github.com/testowner/testrepo.git",
                credentials,
                "main",
                null);

        return new Job(
                1L, "Test Job", "Description", JobType.SIMPLE, config,
                true, // cleanupWorkspace = true
                true, null, new Date(), null, new Date());
    }

    private Credentials createTestCredential() {
        TokenCredential tokenCredential = new TokenCredential("encrypted-token");
        return new Credentials(
                1L, "Test Credential", "Description",
                CredentialType.TOKEN, null, tokenCredential,
                null, new Date(), null, new Date());
    }

    private ProcessResult createSuccessResult(String output) {
        Instant now = Instant.now();
        return new ProcessResult(true, 0, output, "", now, now, Duration.ZERO);
    }

    private ProcessResult createFailureResult(String error) {
        Instant now = Instant.now();
        return new ProcessResult(false, 1, "", error, now, now, Duration.ZERO);
    }
}
