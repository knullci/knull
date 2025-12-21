package org.knullci.knull.application.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.knullci.knull.application.command.CreateJobCommand;
import org.knullci.knull.domain.enums.JobType;
import org.knullci.knull.domain.model.Credentials;
import org.knullci.knull.domain.model.Job;
import org.knullci.knull.domain.repository.CredentialRepository;
import org.knullci.knull.domain.repository.JobRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateJobCommandHandlerImplTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private CredentialRepository credentialRepository;

    @InjectMocks
    private CreateJobCommandHandlerImpl handler;

    @Test
    void testHandle_WithValidCommand_ShouldCreateJob() {
        // Arrange
        CreateJobCommand command = new CreateJobCommand(
                "Test Job",
                "Test Description",
                JobType.SIMPLE,
                "https://github.com/test/repo.git",
                null,
                "main",
                null,
                "knull.yaml");

        doNothing().when(jobRepository).saveJob(any(Job.class));

        // Act
        handler.handle(command);

        // Assert
        verify(jobRepository).saveJob(any(Job.class));
        verify(credentialRepository, never()).findById(anyLong());
    }

    @Test
    void testHandle_WithCredentialId_ShouldFetchCredentials() {
        // Arrange
        Long credentialId = 123L;
        Credentials mockCredentials = mock(Credentials.class);
        // ID mock not needed for this test scenario as it's not accessed by the handler
        // We don't need setUsername or other setters as we are mocking behavior if
        // accessed,
        // or just passing the object around.

        CreateJobCommand command = new CreateJobCommand(
                "Test Job",
                "Test Description",
                JobType.SIMPLE,
                "https://github.com/test/repo.git",
                credentialId,
                "main",
                null,
                "knull.yaml");

        when(credentialRepository.findById(credentialId)).thenReturn(Optional.of(mockCredentials));
        doNothing().when(jobRepository).saveJob(any(Job.class));

        // Act
        handler.handle(command);

        // Assert
        verify(credentialRepository).findById(credentialId);
        verify(jobRepository).saveJob(any(Job.class));
    }

    @Test
    void testHandle_WithInvalidCredentialId_ShouldThrowException() {
        // Arrange
        Long credentialId = 999L;
        CreateJobCommand command = new CreateJobCommand(
                "Test Job",
                "Test Description",
                JobType.SIMPLE,
                "https://github.com/test/repo.git",
                credentialId,
                "main",
                null,
                "knull.yaml");

        when(credentialRepository.findById(credentialId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> handler.handle(command));
        assertEquals("Credential not found with id: 999", exception.getMessage());
        verify(credentialRepository).findById(credentialId);
        verify(jobRepository, never()).saveJob(any(Job.class));
    }

    @Test
    void testHandle_WithMultiBranchJob_ShouldCreateSuccessfully() {
        // Arrange
        CreateJobCommand command = new CreateJobCommand(
                "Multi Branch Job",
                "Multi Branch Description",
                JobType.MULTIBRANCH,
                "https://github.com/test/repo.git",
                null,
                null,
                "feature/*",
                "knull.yaml");

        doNothing().when(jobRepository).saveJob(any(Job.class));

        // Act
        handler.handle(command);

        // Assert
        verify(jobRepository).saveJob(any(Job.class));
    }
}
