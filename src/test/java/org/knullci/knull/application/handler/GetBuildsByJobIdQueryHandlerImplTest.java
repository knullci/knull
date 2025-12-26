package org.knullci.knull.application.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.knullci.knull.application.dto.BuildDto;
import org.knullci.knull.application.query.GetBuildsByJobIdQuery;
import org.knullci.knull.domain.enums.BuildStatus;
import org.knullci.knull.domain.model.Build;
import org.knullci.knull.domain.repository.BuildRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetBuildsByJobIdQueryHandlerImplTest {

    @Mock
    private BuildRepository buildRepository;

    @InjectMocks
    private GetBuildsByJobIdQueryHandlerImpl handler;

    @Test
    void testHandle_WithMultipleBuilds_ShouldReturnAllBuilds() {
        // Arrange
        Long jobId = 1L;
        Build build1 = createTestBuild(1L, jobId, BuildStatus.SUCCESS);
        Build build2 = createTestBuild(2L, jobId, BuildStatus.FAILURE);

        when(buildRepository.findByJobId(jobId)).thenReturn(Arrays.asList(build1, build2));

        // Act
        List<BuildDto> result = handler.handle(new GetBuildsByJobIdQuery(jobId));

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(buildRepository).findByJobId(jobId);
    }

    @Test
    void testHandle_WithNoBuilds_ShouldReturnEmptyList() {
        // Arrange
        Long jobId = 1L;
        when(buildRepository.findByJobId(jobId)).thenReturn(Collections.emptyList());

        // Act
        List<BuildDto> result = handler.handle(new GetBuildsByJobIdQuery(jobId));

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(buildRepository).findByJobId(jobId);
    }

    @Test
    void testHandle_ShouldMapBuildPropertiesCorrectly() {
        // Arrange
        Long jobId = 1L;
        Build build = createTestBuild(1L, jobId, BuildStatus.SUCCESS);
        build.setCommitSha("abc123");
        build.setCommitMessage("Test commit");
        build.setBranch("main");
        build.setRepositoryUrl("https://github.com/test/repo");
        build.setRepositoryOwner("testowner");
        build.setRepositoryName("testrepo");
        build.setTriggeredBy("testuser");

        when(buildRepository.findByJobId(jobId)).thenReturn(Collections.singletonList(build));

        // Act
        List<BuildDto> result = handler.handle(new GetBuildsByJobIdQuery(jobId));

        // Assert
        assertEquals(1, result.size());
        BuildDto dto = result.get(0);
        assertEquals(1L, dto.getId());
        assertEquals(jobId, dto.getJobId());
        assertEquals("abc123", dto.getCommitSha());
        assertEquals("Test commit", dto.getCommitMessage());
        assertEquals("main", dto.getBranch());
        assertEquals("https://github.com/test/repo", dto.getRepositoryUrl());
        assertEquals("testowner", dto.getRepositoryOwner());
        assertEquals("testrepo", dto.getRepositoryName());
        assertEquals(BuildStatus.SUCCESS, dto.getStatus());
        assertEquals("testuser", dto.getTriggeredBy());
    }

    @Test
    void testHandle_WithDifferentJobId_ShouldQueryCorrectJobId() {
        // Arrange
        Long jobId = 999L;
        when(buildRepository.findByJobId(jobId)).thenReturn(Collections.emptyList());

        // Act
        handler.handle(new GetBuildsByJobIdQuery(jobId));

        // Assert
        verify(buildRepository).findByJobId(999L);
    }

    @Test
    void testHandle_WithInProgressBuild_ShouldReturnCorrectStatus() {
        // Arrange
        Long jobId = 1L;
        Build build = createTestBuild(1L, jobId, BuildStatus.IN_PROGRESS);
        build.setCompletedAt(null); // In progress builds have no completion time

        when(buildRepository.findByJobId(jobId)).thenReturn(Collections.singletonList(build));

        // Act
        List<BuildDto> result = handler.handle(new GetBuildsByJobIdQuery(jobId));

        // Assert
        assertEquals(1, result.size());
        assertEquals(BuildStatus.IN_PROGRESS, result.get(0).getStatus());
        assertNull(result.get(0).getCompletedAt());
    }

    private Build createTestBuild(Long id, Long jobId, BuildStatus status) {
        Build build = new Build();
        build.setId(id);
        build.setJobId(jobId);
        build.setJobName("Test Job");
        build.setCommitSha("abc123");
        build.setCommitMessage("Test commit");
        build.setBranch("main");
        build.setRepositoryUrl("https://github.com/test/repo");
        build.setRepositoryOwner("testowner");
        build.setRepositoryName("testrepo");
        build.setStatus(status);
        build.setBuildLog("Build log content");
        build.setStartedAt(new Date());
        build.setCompletedAt(new Date());
        build.setDuration(5000L);
        build.setTriggeredBy("testuser");
        return build;
    }
}
