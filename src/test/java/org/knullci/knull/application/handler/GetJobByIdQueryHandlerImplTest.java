package org.knullci.knull.application.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.knullci.knull.application.dto.JobDetailDto;
import org.knullci.knull.application.query.GetJobByIdQuery;
import org.knullci.knull.domain.model.Job;
import org.knullci.knull.domain.repository.JobRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetJobByIdQueryHandlerImplTest {

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private GetJobByIdQueryHandlerImpl handler;

    @Test
    void testHandle_WithValidId_ShouldReturnJobDetailDto() {
        // Arrange
        Long jobId = 123L;
        GetJobByIdQuery query = new GetJobByIdQuery(jobId);

        Job mockJob = mock(Job.class);
        when(mockJob.getId()).thenReturn(jobId);
        when(mockJob.getName()).thenReturn("Test Job");
        when(mockJob.getDescription()).thenReturn("Test Description");
        // We need to mock JobConfig if accessed, assuming JobFactory handles nulls or
        // we default
        // For simple DTO conversion, often we just need id/name/desc.
        // If JobFactory.toDetailDto accesses other fields, we should mock them too if
        // needed.
        // Assuming toDetailDto handles basic fields.

        when(jobRepository.getJobId(jobId)).thenReturn(Optional.of(mockJob));

        // Act
        JobDetailDto result = handler.handle(query);

        // Assert
        assertNotNull(result);
        verify(jobRepository).getJobId(jobId);
    }

    @Test
    void testHandle_WithInvalidId_ShouldThrowException() {
        // Arrange
        Long jobId = 999L;
        GetJobByIdQuery query = new GetJobByIdQuery(jobId);

        when(jobRepository.getJobId(jobId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> handler.handle(query));
        assertEquals("Job not found with id: 999", exception.getMessage());
        verify(jobRepository).getJobId(jobId);
    }

    @Test
    void testHandle_ShouldCallRepositoryOnce() {
        // Arrange
        Long jobId = 456L;
        GetJobByIdQuery query = new GetJobByIdQuery(jobId);

        Job mockJob = mock(Job.class);
        when(mockJob.getId()).thenReturn(jobId);

        when(jobRepository.getJobId(jobId)).thenReturn(Optional.of(mockJob));

        // Act
        handler.handle(query);

        // Assert
        verify(jobRepository, times(1)).getJobId(jobId);
    }
}
