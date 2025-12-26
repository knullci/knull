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
import org.knullci.knull.application.command.GithubWebhookCommand;
import org.knullci.knull.application.dto.*;
import org.knullci.knull.application.interfaces.ExecuteBuildCommandHandler;
import org.knullci.knull.domain.enums.JobType;
import org.knullci.knull.domain.model.Job;
import org.knullci.knull.domain.model.SimpleJobConfig;
import org.knullci.knull.domain.repository.JobRepository;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GithubWebhookCommandHandlerImplTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private ExecuteBuildCommandHandler executeBuildCommandHandler;

    @InjectMocks
    private GithubWebhookCommandHandlerImpl handler;

    @Captor
    private ArgumentCaptor<ExecuteBuildCommand> buildCommandCaptor;

    private GithubWebhookRequestDto webhookRequest;
    private Job testJob;

    @BeforeEach
    void setUp() {
        SimpleJobConfig config = new SimpleJobConfig(1L, "https://github.com/testowner/testrepo", null, "main",
                "knull.yaml");
        testJob = new Job(
                1L, "Test Job", "Description", JobType.SIMPLE, config,
                false, true, null, new Date(), null, new Date());

        // Create webhook request using setters
        GithubWebhookOwner owner = new GithubWebhookOwner();
        owner.setName("testowner");
        owner.setLogin("testowner");

        GithubWebhookRepository repository = new GithubWebhookRepository();
        repository.setName("testrepo");
        repository.setHtmlUrl("https://github.com/testowner/testrepo");
        repository.setOwner(owner);

        GithubWebhookHeadCommit headCommit = new GithubWebhookHeadCommit();
        headCommit.setId("abc123");
        headCommit.setMessage("Test commit message");

        GithubWebhookSender sender = new GithubWebhookSender();
        sender.setLogin("testuser");

        webhookRequest = new GithubWebhookRequestDto();
        webhookRequest.setRef("refs/heads/main");
        webhookRequest.setRepository(repository);
        webhookRequest.setHeadCommit(headCommit);
        webhookRequest.setSender(sender);
    }

    @Test
    void testHandle_WhenJobExists_ShouldTriggerBuild() {
        // Arrange
        when(jobRepository.getJobByRepoName("testrepo")).thenReturn(Optional.of(testJob));
        doNothing().when(executeBuildCommandHandler).handle(any(ExecuteBuildCommand.class));

        GithubWebhookCommand command = new GithubWebhookCommand(webhookRequest);

        // Act
        handler.handle(command);

        // Assert
        verify(jobRepository).getJobByRepoName("testrepo");
        verify(executeBuildCommandHandler).handle(buildCommandCaptor.capture());

        ExecuteBuildCommand capturedCommand = buildCommandCaptor.getValue();
        assertEquals(testJob, capturedCommand.getJob());
        assertEquals("abc123", capturedCommand.getCommitSha());
        assertEquals("Test commit message", capturedCommand.getCommitMessage());
        assertEquals("main", capturedCommand.getBranch());
    }

    @Test
    void testHandle_WhenJobNotFound_ShouldNotTriggerBuild() {
        // Arrange
        when(jobRepository.getJobByRepoName("testrepo")).thenReturn(Optional.empty());

        GithubWebhookCommand command = new GithubWebhookCommand(webhookRequest);

        // Act
        GithubWebhookResponseDto result = handler.handle(command);

        // Assert
        assertNull(result);
        verify(jobRepository).getJobByRepoName("testrepo");
        verify(executeBuildCommandHandler, never()).handle(any(ExecuteBuildCommand.class));
    }

    @Test
    void testHandle_ShouldExtractBranchNameFromRef() {
        // Arrange
        when(jobRepository.getJobByRepoName("testrepo")).thenReturn(Optional.of(testJob));
        doNothing().when(executeBuildCommandHandler).handle(buildCommandCaptor.capture());

        GithubWebhookCommand command = new GithubWebhookCommand(webhookRequest);

        // Act
        handler.handle(command);

        // Assert
        ExecuteBuildCommand capturedCommand = buildCommandCaptor.getValue();
        assertEquals("main", capturedCommand.getBranch()); // refs/heads/main -> main
    }

    @Test
    void testHandle_ShouldPassCorrectRepositoryInfo() {
        // Arrange
        when(jobRepository.getJobByRepoName("testrepo")).thenReturn(Optional.of(testJob));
        doNothing().when(executeBuildCommandHandler).handle(buildCommandCaptor.capture());

        GithubWebhookCommand command = new GithubWebhookCommand(webhookRequest);

        // Act
        handler.handle(command);

        // Assert
        ExecuteBuildCommand capturedCommand = buildCommandCaptor.getValue();
        assertEquals("testowner", capturedCommand.getRepositoryOwner());
        assertEquals("testrepo", capturedCommand.getRepositoryName());
        assertEquals("https://github.com/testowner/testrepo", capturedCommand.getRepositoryUrl());
    }

    @Test
    void testHandle_ShouldPassSenderAsTriggeredBy() {
        // Arrange
        when(jobRepository.getJobByRepoName("testrepo")).thenReturn(Optional.of(testJob));
        doNothing().when(executeBuildCommandHandler).handle(buildCommandCaptor.capture());

        GithubWebhookCommand command = new GithubWebhookCommand(webhookRequest);

        // Act
        handler.handle(command);

        // Assert
        ExecuteBuildCommand capturedCommand = buildCommandCaptor.getValue();
        assertEquals("testuser", capturedCommand.getTriggeredBy());
    }

    @Test
    void testHandle_WithFeatureBranch_ShouldExtractCorrectBranchName() {
        // Arrange
        GithubWebhookOwner owner = new GithubWebhookOwner();
        owner.setName("testowner");

        GithubWebhookRepository repository = new GithubWebhookRepository();
        repository.setName("testrepo");
        repository.setHtmlUrl("https://github.com/testowner/testrepo");
        repository.setOwner(owner);

        GithubWebhookHeadCommit headCommit = new GithubWebhookHeadCommit();
        headCommit.setId("def456");
        headCommit.setMessage("Feature commit");

        GithubWebhookSender sender = new GithubWebhookSender();
        sender.setLogin("developer");

        GithubWebhookRequestDto featureBranchWebhook = new GithubWebhookRequestDto();
        featureBranchWebhook.setRef("refs/heads/feature/new-feature");
        featureBranchWebhook.setRepository(repository);
        featureBranchWebhook.setHeadCommit(headCommit);
        featureBranchWebhook.setSender(sender);

        when(jobRepository.getJobByRepoName("testrepo")).thenReturn(Optional.of(testJob));
        doNothing().when(executeBuildCommandHandler).handle(buildCommandCaptor.capture());

        GithubWebhookCommand command = new GithubWebhookCommand(featureBranchWebhook);

        // Act
        handler.handle(command);

        // Assert
        ExecuteBuildCommand capturedCommand = buildCommandCaptor.getValue();
        assertEquals("feature/new-feature", capturedCommand.getBranch());
    }
}
