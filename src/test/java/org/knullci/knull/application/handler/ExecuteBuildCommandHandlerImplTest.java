package org.knullci.knull.application.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.knullci.knull.application.command.ExecuteBuildCommand;
import org.knullci.knull.domain.enums.BuildStatus;
import org.knullci.knull.domain.enums.JobType;
import org.knullci.knull.domain.model.Build;
import org.knullci.knull.domain.model.Job;
import org.knullci.knull.domain.model.SimpleJobConfig;
import org.knullci.knull.domain.repository.BuildRepository;
import org.knullci.knull.infrastructure.dto.UpdateCommitStatusDto;
import org.knullci.knull.infrastructure.enums.GHCommitState;
import org.knullci.knull.infrastructure.service.BuildExecutorService;
import org.knullci.knull.infrastructure.service.GithubService;

import java.util.ArrayList;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExecuteBuildCommandHandlerImplTest {

    @Mock
    private BuildRepository buildRepository;

    @Mock
    private GithubService githubService;

    @Mock
    private BuildExecutorService buildExecutorService;

    @InjectMocks
    private ExecuteBuildCommandHandlerImpl handler;

    @Captor
    private ArgumentCaptor<Build> buildCaptor;

    @Captor
    private ArgumentCaptor<UpdateCommitStatusDto> statusCaptor;

    private Job testJob;
    private ExecuteBuildCommand testCommand;

    @BeforeEach
    void setUp() {
        SimpleJobConfig config = new SimpleJobConfig(1L, "https://github.com/testowner/testrepo", null, "main",
                "knull.yaml");
        testJob = new Job(
                1L, "Test Job", "Description", JobType.SIMPLE, config,
                false, true, null, new Date(), null, new Date());

        testCommand = new ExecuteBuildCommand(
                testJob,
                "abc123",
                "Test commit message",
                "main",
                "testowner",
                "testrepo",
                "https://github.com/testowner/testrepo",
                "testuser");
    }

    @Test
    void testHandle_WhenBuildSucceeds_ShouldUpdateStatusToSuccess() {
        // Arrange
        Build savedBuild = createTestBuild();

        when(buildRepository.saveBuild(any(Build.class))).thenReturn(savedBuild);
        doNothing().when(githubService).updateCommitStatus(any(UpdateCommitStatusDto.class));
        doNothing().when(buildExecutorService).executeBuild(any(Build.class), any(Job.class));
        doNothing().when(buildRepository).updateBuild(any(Build.class));

        // Act
        handler.handle(testCommand);

        // Assert
        verify(buildRepository).saveBuild(any(Build.class));
        verify(buildExecutorService).executeBuild(any(Build.class), eq(testJob));
        verify(buildRepository).updateBuild(buildCaptor.capture());

        Build updatedBuild = buildCaptor.getValue();
        assertEquals(BuildStatus.SUCCESS, updatedBuild.getStatus());
        assertNotNull(updatedBuild.getCompletedAt());
    }

    @Test
    void testHandle_WhenBuildFails_ShouldUpdateStatusToFailure() {
        // Arrange
        Build savedBuild = createTestBuild();

        when(buildRepository.saveBuild(any(Build.class))).thenReturn(savedBuild);
        doNothing().when(githubService).updateCommitStatus(any(UpdateCommitStatusDto.class));
        doThrow(new RuntimeException("Build execution failed")).when(buildExecutorService)
                .executeBuild(any(Build.class), any(Job.class));
        doNothing().when(buildRepository).updateBuild(any(Build.class));

        // Act
        handler.handle(testCommand);

        // Assert
        verify(buildRepository).saveBuild(any(Build.class));
        verify(buildRepository).updateBuild(buildCaptor.capture());

        Build updatedBuild = buildCaptor.getValue();
        assertEquals(BuildStatus.FAILURE, updatedBuild.getStatus());
        assertTrue(updatedBuild.getBuildLog().contains("Build failed"));
    }

    @Test
    void testHandle_ShouldSetInitialBuildProperties() {
        // Arrange
        Build savedBuild = createTestBuild();

        when(buildRepository.saveBuild(buildCaptor.capture())).thenReturn(savedBuild);
        doNothing().when(githubService).updateCommitStatus(any(UpdateCommitStatusDto.class));
        doNothing().when(buildExecutorService).executeBuild(any(Build.class), any(Job.class));
        doNothing().when(buildRepository).updateBuild(any(Build.class));

        // Act
        handler.handle(testCommand);

        // Assert
        Build capturedBuild = buildCaptor.getValue();
        assertEquals(testJob.getId(), capturedBuild.getJobId());
        assertEquals(testJob.getName(), capturedBuild.getJobName());
        assertEquals("abc123", capturedBuild.getCommitSha());
        assertEquals("Test commit message", capturedBuild.getCommitMessage());
        assertEquals("main", capturedBuild.getBranch());
        assertEquals("testowner", capturedBuild.getRepositoryOwner());
        assertEquals("testrepo", capturedBuild.getRepositoryName());
        assertEquals(BuildStatus.IN_PROGRESS, capturedBuild.getStatus());
        assertEquals("testuser", capturedBuild.getTriggeredBy());
    }

    @Test
    void testHandle_ShouldUpdateGithubStatusToPending() {
        // Arrange
        Build savedBuild = createTestBuild();

        when(buildRepository.saveBuild(any(Build.class))).thenReturn(savedBuild);
        doNothing().when(githubService).updateCommitStatus(statusCaptor.capture());
        doNothing().when(buildExecutorService).executeBuild(any(Build.class), any(Job.class));
        doNothing().when(buildRepository).updateBuild(any(Build.class));

        // Act
        handler.handle(testCommand);

        // Assert
        UpdateCommitStatusDto firstStatus = statusCaptor.getAllValues().get(0);
        assertEquals("testowner", firstStatus.getOwner());
        assertEquals("testrepo", firstStatus.getRepo());
        assertEquals("abc123", firstStatus.getCommitSha());
        assertEquals(GHCommitState.PENDING, firstStatus.getCommitState());
    }

    @Test
    void testHandle_OnSuccess_ShouldUpdateGithubStatusToSuccess() {
        // Arrange
        Build savedBuild = createTestBuild();

        when(buildRepository.saveBuild(any(Build.class))).thenReturn(savedBuild);
        doNothing().when(githubService).updateCommitStatus(statusCaptor.capture());
        doNothing().when(buildExecutorService).executeBuild(any(Build.class), any(Job.class));
        doNothing().when(buildRepository).updateBuild(any(Build.class));

        // Act
        handler.handle(testCommand);

        // Assert - Second call should be SUCCESS
        UpdateCommitStatusDto successStatus = statusCaptor.getAllValues().get(1);
        assertEquals(GHCommitState.SUCCESS, successStatus.getCommitState());
    }

    @Test
    void testHandle_OnFailure_ShouldUpdateGithubStatusToFailure() {
        // Arrange
        Build savedBuild = createTestBuild();

        when(buildRepository.saveBuild(any(Build.class))).thenReturn(savedBuild);
        doNothing().when(githubService).updateCommitStatus(statusCaptor.capture());
        doThrow(new RuntimeException("Build failed")).when(buildExecutorService)
                .executeBuild(any(Build.class), any(Job.class));
        doNothing().when(buildRepository).updateBuild(any(Build.class));

        // Act
        handler.handle(testCommand);

        // Assert - Second call should be FAILURE
        UpdateCommitStatusDto failureStatus = statusCaptor.getAllValues().get(1);
        assertEquals(GHCommitState.FAILURE, failureStatus.getCommitState());
    }

    private Build createTestBuild() {
        Build build = new Build();
        build.setId(1L);
        build.setJobId(testJob.getId());
        build.setJobName(testJob.getName());
        build.setCommitSha("abc123");
        build.setCommitMessage("Test commit message");
        build.setBranch("main");
        build.setRepositoryUrl("https://github.com/testowner/testrepo");
        build.setRepositoryOwner("testowner");
        build.setRepositoryName("testrepo");
        build.setStatus(BuildStatus.IN_PROGRESS);
        build.setBuildLog("Build started...\n");
        build.setSteps(new ArrayList<>());
        build.setStartedAt(new Date());
        build.setTriggeredBy("testuser");
        return build;
    }
}
