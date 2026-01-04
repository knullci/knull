package org.knullci.knull.infrastructure.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
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
import org.knullci.knull.domain.repository.SecretFileRepository;
import org.knullci.knull.proto.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NecroswordExecutorTest {

    @Mock
    private CredentialRepository credentialRepository;

    @Mock
    private BuildRepository buildRepository;

    @Mock
    private SecretFileRepository secretFileRepository;

    @Mock
    private EncryptionService encryptionService;

    @Mock
    private ObjectMapper yamlObjectMapper;

    @Mock
    private ManagedChannel managedChannel;

    @Mock
    private ExecutorServiceGrpc.ExecutorServiceBlockingStub blockingStub;

    @Mock
    private ExecutorServiceGrpc.ExecutorServiceStub asyncStub;

    private NecroswordExecutor necroswordExecutor;

    @BeforeEach
    void setUp() {
        necroswordExecutor = new NecroswordExecutor(
                credentialRepository,
                encryptionService,
                buildRepository,
                secretFileRepository,
                yamlObjectMapper);

        // Set configuration values via reflection
        ReflectionTestUtils.setField(necroswordExecutor, "workspaceBasePath", "/tmp/test-workspace");
        ReflectionTestUtils.setField(necroswordExecutor, "grpcHost", "localhost");
        ReflectionTestUtils.setField(necroswordExecutor, "grpcPort", 8081);
        ReflectionTestUtils.setField(necroswordExecutor, "useTls", false);
        ReflectionTestUtils.setField(necroswordExecutor, "maxInboundMessageSize", 16777216);

        // Set mocked stubs via reflection
        ReflectionTestUtils.setField(necroswordExecutor, "channel", managedChannel);
        ReflectionTestUtils.setField(necroswordExecutor, "blockingStub", blockingStub);
        ReflectionTestUtils.setField(necroswordExecutor, "asyncStub", asyncStub);
    }

    // ==================== isHealthy() Tests ====================

    @Test
    void testIsHealthy_WhenServerHealthy_ShouldReturnTrue() {
        // Arrange
        HealthResponse healthResponse = HealthResponse.newBuilder()
                .setStatus("healthy")
                .build();

        when(blockingStub.withDeadlineAfter(anyLong(), any())).thenReturn(blockingStub);
        when(blockingStub.health(any(HealthRequest.class))).thenReturn(healthResponse);

        // Act
        boolean result = necroswordExecutor.isHealthy();

        // Assert
        assertTrue(result);
        verify(blockingStub).health(any(HealthRequest.class));
    }

    @Test
    void testIsHealthy_WhenServerUnhealthy_ShouldReturnFalse() {
        // Arrange
        HealthResponse healthResponse = HealthResponse.newBuilder()
                .setStatus("unhealthy")
                .build();

        when(blockingStub.withDeadlineAfter(anyLong(), any())).thenReturn(blockingStub);
        when(blockingStub.health(any(HealthRequest.class))).thenReturn(healthResponse);

        // Act
        boolean result = necroswordExecutor.isHealthy();

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsHealthy_WhenExceptionThrown_ShouldReturnFalse() {
        // Arrange
        when(blockingStub.withDeadlineAfter(anyLong(), any())).thenReturn(blockingStub);
        when(blockingStub.health(any(HealthRequest.class)))
                .thenThrow(new StatusRuntimeException(Status.UNAVAILABLE));

        // Act
        boolean result = necroswordExecutor.isHealthy();

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsHealthy_WhenNullStatus_ShouldReturnFalse() {
        // Arrange
        HealthResponse healthResponse = HealthResponse.newBuilder()
                .setStatus("")
                .build();

        when(blockingStub.withDeadlineAfter(anyLong(), any())).thenReturn(blockingStub);
        when(blockingStub.health(any(HealthRequest.class))).thenReturn(healthResponse);

        // Act
        boolean result = necroswordExecutor.isHealthy();

        // Assert
        assertFalse(result);
    }

    // ==================== getRunningProcessCount() Tests ====================

    @Test
    void testGetRunningProcessCount_WhenProcessesExist_ShouldReturnCount() {
        // Arrange
        GetProcessesResponse response = GetProcessesResponse.newBuilder()
                .setCount(5)
                .build();

        when(blockingStub.withDeadlineAfter(anyLong(), any())).thenReturn(blockingStub);
        when(blockingStub.getRunningProcesses(any(GetProcessesRequest.class))).thenReturn(response);

        // Act
        int result = necroswordExecutor.getRunningProcessCount();

        // Assert
        assertEquals(5, result);
        verify(blockingStub).getRunningProcesses(any(GetProcessesRequest.class));
    }

    @Test
    void testGetRunningProcessCount_WhenNoProcesses_ShouldReturnZero() {
        // Arrange
        GetProcessesResponse response = GetProcessesResponse.newBuilder()
                .setCount(0)
                .build();

        when(blockingStub.withDeadlineAfter(anyLong(), any())).thenReturn(blockingStub);
        when(blockingStub.getRunningProcesses(any(GetProcessesRequest.class))).thenReturn(response);

        // Act
        int result = necroswordExecutor.getRunningProcessCount();

        // Assert
        assertEquals(0, result);
    }

    @Test
    void testGetRunningProcessCount_WhenExceptionThrown_ShouldReturnNegativeOne() {
        // Arrange
        when(blockingStub.withDeadlineAfter(anyLong(), any())).thenReturn(blockingStub);
        when(blockingStub.getRunningProcesses(any(GetProcessesRequest.class)))
                .thenThrow(new StatusRuntimeException(Status.UNAVAILABLE));

        // Act
        int result = necroswordExecutor.getRunningProcessCount();

        // Assert
        assertEquals(-1, result);
    }

    // ==================== buildAuthenticatedUrl() Tests ====================

    @Test
    void testBuildAuthenticatedUrl_WithTokenCredential_ShouldReturnAuthenticatedUrl() throws Exception {
        // Arrange
        Credentials credentials = createTokenCredentials();
        when(credentialRepository.findById(1L)).thenReturn(Optional.of(credentials));
        when(encryptionService.decrypt("encrypted-token")).thenReturn("my-secret-token");

        // Use reflection to test private method
        String result = ReflectionTestUtils.invokeMethod(necroswordExecutor, "buildAuthenticatedUrl",
                "https://github.com/owner/repo.git", credentials);

        // Assert
        assertEquals("https://my-secret-token@github.com/owner/repo.git", result);
    }

    @Test
    void testBuildAuthenticatedUrl_WithUsernamePassword_ShouldReturnAuthenticatedUrl() throws Exception {
        // Arrange
        Credentials credentials = createUsernamePasswordCredentials();
        when(credentialRepository.findById(1L)).thenReturn(Optional.of(credentials));
        when(encryptionService.decrypt("encrypted-password")).thenReturn("my-secret-password");

        // Use reflection to test private method
        String result = ReflectionTestUtils.invokeMethod(necroswordExecutor, "buildAuthenticatedUrl",
                "https://github.com/owner/repo.git", credentials);

        // Assert
        assertEquals("https://testuser:my-secret-password@github.com/owner/repo.git", result);
    }

    @Test
    void testBuildAuthenticatedUrl_WithNullCredentials_ShouldThrowException() {
        // Arrange & Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> ReflectionTestUtils.invokeMethod(necroswordExecutor, "buildAuthenticatedUrl",
                        "https://github.com/owner/repo.git", (Credentials) null));

        assertTrue(exception.getMessage().contains("No credentials configured"));
    }

    @Test
    void testBuildAuthenticatedUrl_WithNonExistentCredential_ShouldThrowException() {
        // Arrange
        Credentials credentials = createTokenCredentials();
        when(credentialRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> ReflectionTestUtils.invokeMethod(necroswordExecutor, "buildAuthenticatedUrl",
                        "https://github.com/owner/repo.git", credentials));

        assertTrue(exception.getMessage().contains("Credential not found"));
    }

    @Test
    void testBuildAuthenticatedUrl_WithHttpUrl_ShouldReturnHttpAuthenticatedUrl() throws Exception {
        // Arrange
        Credentials credentials = createTokenCredentials();
        when(credentialRepository.findById(1L)).thenReturn(Optional.of(credentials));
        when(encryptionService.decrypt("encrypted-token")).thenReturn("my-secret-token");

        // Use reflection to test private method
        String result = ReflectionTestUtils.invokeMethod(necroswordExecutor, "buildAuthenticatedUrl",
                "http://github.com/owner/repo.git", credentials);

        // Assert
        assertEquals("http://my-secret-token@github.com/owner/repo.git", result);
    }

    // ==================== cleanupIfRequired() Tests ====================

    @Test
    void testCleanupIfRequired_WhenCleanupDisabled_ShouldNotCleanup() {
        // Arrange
        Build build = createTestBuild();
        Job job = createTestJobWithCleanup(false);

        // Act - Use reflection to test private method
        ReflectionTestUtils.invokeMethod(necroswordExecutor, "cleanupIfRequired",
                build, job, "/tmp/test-workspace/build-1");

        // Assert - No exception and build steps should not contain cleanup step
        assertTrue(build.getSteps().isEmpty());
    }

    @Test
    void testCleanupIfRequired_WhenCleanupEnabled_ShouldAddCleanupStep() throws Exception {
        // Arrange
        Build build = createTestBuild();
        Job job = createTestJobWithCleanup(true);

        // Create a temporary directory that will be cleaned up
        java.nio.file.Path tempDir = java.nio.file.Files.createTempDirectory("test-cleanup");

        // Act
        ReflectionTestUtils.invokeMethod(necroswordExecutor, "cleanupIfRequired",
                build, job, tempDir.toString());

        // Assert - Cleanup step should be added
        assertEquals(1, build.getSteps().size());
        assertEquals("Cleanup Workspace", build.getSteps().get(0).getName());
    }

    // ==================== appendToBuildLog() Tests ====================

    @Test
    void testAppendToBuildLog_WithExistingLog_ShouldAppend() {
        // Arrange
        Build build = createTestBuild();
        build.setBuildLog("Existing log\n");

        // Act
        ReflectionTestUtils.invokeMethod(necroswordExecutor, "appendToBuildLog",
                build, "New line");

        // Assert
        assertEquals("Existing log\nNew line", build.getBuildLog());
        verify(buildRepository).updateBuild(build);
    }

    @Test
    void testAppendToBuildLog_WithNullLog_ShouldCreateNew() {
        // Arrange
        Build build = createTestBuild();
        build.setBuildLog(null);

        // Act
        ReflectionTestUtils.invokeMethod(necroswordExecutor, "appendToBuildLog",
                build, "First line");

        // Assert
        assertEquals("First line", build.getBuildLog());
        verify(buildRepository).updateBuild(build);
    }

    // ==================== shutdown() Tests ====================

    @Test
    void testShutdown_WhenChannelNotShutdown_ShouldShutdownGracefully() throws Exception {
        // Arrange
        when(managedChannel.isShutdown()).thenReturn(false);
        when(managedChannel.shutdown()).thenReturn(managedChannel);
        when(managedChannel.awaitTermination(anyLong(), any())).thenReturn(true);

        // Act
        necroswordExecutor.shutdown();

        // Assert
        verify(managedChannel).shutdown();
        verify(managedChannel).awaitTermination(anyLong(), any());
        verify(managedChannel, never()).shutdownNow();
    }

    @Test
    void testShutdown_WhenTerminationTimeouts_ShouldForceShutdown() throws Exception {
        // Arrange
        when(managedChannel.isShutdown()).thenReturn(false);
        when(managedChannel.shutdown()).thenReturn(managedChannel);
        when(managedChannel.awaitTermination(anyLong(), any())).thenReturn(false);
        when(managedChannel.shutdownNow()).thenReturn(managedChannel);

        // Act
        necroswordExecutor.shutdown();

        // Assert
        verify(managedChannel).shutdown();
        verify(managedChannel).shutdownNow();
    }

    @Test
    void testShutdown_WhenChannelAlreadyShutdown_ShouldDoNothing() {
        // Arrange
        when(managedChannel.isShutdown()).thenReturn(true);

        // Act
        necroswordExecutor.shutdown();

        // Assert
        verify(managedChannel, never()).shutdown();
    }

    @Test
    void testShutdown_WhenChannelIsNull_ShouldNotThrowException() {
        // Arrange
        ReflectionTestUtils.setField(necroswordExecutor, "channel", null);

        // Act & Assert - Should not throw
        assertDoesNotThrow(() -> necroswordExecutor.shutdown());
    }

    // ==================== Helper Methods ====================

    private Build createTestBuild() {
        Build build = new Build();
        build.setId(1L);
        build.setJobId(1L);
        build.setJobName("Test Job");
        build.setCommitSha("abc123");
        build.setCommitMessage("Test commit");
        build.setBranch("main");
        build.setRepositoryUrl("https://github.com/testowner/testrepo");
        build.setRepositoryOwner("testowner");
        build.setRepositoryName("testrepo");
        build.setBuildLog("");
        build.setSteps(new ArrayList<>());
        build.setStartedAt(new Date());
        return build;
    }

    private Job createTestJobWithCleanup(boolean cleanupWorkspace) {
        SimpleJobConfig config = new SimpleJobConfig(1L, "https://github.com/owner/repo", null, "main", "knull.yaml");
        return new Job(1L, "Test Job", "Description", JobType.SIMPLE, config,
                cleanupWorkspace, true, null, new Date(), null, new Date());
    }

    private Credentials createTokenCredentials() {
        TokenCredential tokenCred = new TokenCredential("encrypted-token");
        return new Credentials(1L, "Test Token", "Test Description",
                CredentialType.TOKEN, null, tokenCred, null, new Date(), null, new Date());
    }

    private Credentials createUsernamePasswordCredentials() {
        UsernamePasswordCredential upCred = new UsernamePasswordCredential("testuser", "encrypted-password");
        return new Credentials(1L, "Test UP", "Test Description",
                CredentialType.USERNAME_PASSWORD, upCred, null, null, new Date(), null, new Date());
    }
}
