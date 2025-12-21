package org.knullci.knull.application.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.knullci.knull.application.command.DeleteJobCommand;
import org.knullci.knull.domain.repository.JobRepository;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteJobCommandHandlerImplTest {

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private DeleteJobCommandHandlerImpl handler;

    @Test
    void testHandle_WithValidId_ShouldDeleteJob() {
        // Arrange
        Long jobId = 123L;
        DeleteJobCommand command = new DeleteJobCommand(jobId);
        doNothing().when(jobRepository).deleteJobById(jobId);

        // Act
        handler.handle(command);

        // Assert
        verify(jobRepository).deleteJobById(jobId);
    }

    @Test
    void testHandle_ShouldCallDeleteOnce() {
        // Arrange
        DeleteJobCommand command = new DeleteJobCommand(456L);
        doNothing().when(jobRepository).deleteJobById(anyLong());

        // Act
        handler.handle(command);

        // Assert
        verify(jobRepository, times(1)).deleteJobById(456L);
    }

    @Test
    void testHandle_WithDifferentIds_ShouldDeleteCorrectOne() {
        // Arrange
        DeleteJobCommand command = new DeleteJobCommand(789L);
        doNothing().when(jobRepository).deleteJobById(anyLong());

        // Act
        handler.handle(command);

        // Assert
        verify(jobRepository).deleteJobById(789L);
        verify(jobRepository, never()).deleteJobById(123L);
        verify(jobRepository, never()).deleteJobById(456L);
    }

    @Test
    void testHandle_WhenRepositoryThrowsException_ShouldPropagateException() {
        // Arrange
        DeleteJobCommand command = new DeleteJobCommand(999L);
        doThrow(new RuntimeException("Database error")).when(jobRepository).deleteJobById(999L);

        // Act & Assert
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> handler.handle(command));
        verify(jobRepository).deleteJobById(999L);
    }
}
