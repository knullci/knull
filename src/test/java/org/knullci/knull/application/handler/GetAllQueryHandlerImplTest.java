package org.knullci.knull.application.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.knullci.knull.application.dto.JobDto;
import org.knullci.knull.application.query.GetAllJobQuery;
import org.knullci.knull.domain.enums.JobType;
import org.knullci.knull.domain.model.Job;
import org.knullci.knull.domain.model.SimpleJobConfig;
import org.knullci.knull.domain.repository.JobRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAllQueryHandlerImplTest {

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private GetAllQueryHandlerImpl handler;

    @Test
    void testHandle_WithMultipleJobs_ShouldReturnAllJobs() {
        // Arrange
        Job job1 = createTestJob(1L, "Job 1", JobType.SIMPLE);
        Job job2 = createTestJob(2L, "Job 2", JobType.MULTIBRANCH);

        when(jobRepository.getAllJobs()).thenReturn(Arrays.asList(job1, job2));

        // Act
        List<JobDto> result = handler.handle(new GetAllJobQuery());

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(jobRepository).getAllJobs();
    }

    @Test
    void testHandle_WithNoJobs_ShouldReturnEmptyList() {
        // Arrange
        when(jobRepository.getAllJobs()).thenReturn(Collections.emptyList());

        // Act
        List<JobDto> result = handler.handle(new GetAllJobQuery());

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(jobRepository).getAllJobs();
    }

    @Test
    void testHandle_WithSingleJob_ShouldReturnSingleJobDto() {
        // Arrange
        Job job = createTestJob(1L, "Single Job", JobType.SIMPLE);
        when(jobRepository.getAllJobs()).thenReturn(Collections.singletonList(job));

        // Act
        List<JobDto> result = handler.handle(new GetAllJobQuery());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Single Job", result.get(0).getName());
    }

    @Test
    void testHandle_ShouldCallRepositoryOnce() {
        // Arrange
        when(jobRepository.getAllJobs()).thenReturn(Collections.emptyList());

        // Act
        handler.handle(new GetAllJobQuery());

        // Assert
        verify(jobRepository, times(1)).getAllJobs();
    }

    private Job createTestJob(Long id, String name, JobType type) {
        SimpleJobConfig config = new SimpleJobConfig(
                id,
                "https://github.com/test/repo.git",
                null,
                "main",
                "knull.yaml");

        return new Job(
                id, name, "Test Description", type, config,
                true, true, null, new Date(), null, new Date());
    }
}
