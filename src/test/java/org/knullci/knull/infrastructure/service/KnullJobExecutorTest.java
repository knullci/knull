package org.knullci.knull.infrastructure.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.knullci.knull.domain.enums.JobType;
import org.knullci.knull.domain.model.Job;
import org.knullci.knull.domain.model.SimpleJobConfig;
import org.knullci.knull.infrastructure.dto.ProcessResult;
import org.knullci.knull.infrastructure.knullpojo.v1.JobConfigYaml;
import org.knullci.knull.infrastructure.knullpojo.v1.JobStep;
import org.knullci.knull.infrastructure.knullpojo.v1.JobYaml;
import org.knullci.knull.infrastructure.knullpojo.v1.RunCommand;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KnullJobExecutorTest {

    @Mock
    private StepExecutor stepExecutor;

    @Mock
    private ProcessRunner processRunner;

    @Mock
    private ObjectMapper objectMapper;

    private KnullJobExecutor jobExecutor;

    @BeforeEach
    void setUp() {
        jobExecutor = new KnullJobExecutor(stepExecutor, processRunner, objectMapper);
    }

    @Test
    void testExecute_WithSimpleJob_ShouldCloneAndExecuteSteps() throws Exception {
        // Arrange
        Job job = createSimpleJob();
        ProcessResult successResult = createSuccessResult("Cloned successfully");

        JobYaml jobYaml = createJobYaml();

        when(processRunner.run(any(RunCommand.class))).thenReturn(successResult);
        when(objectMapper.readValue(any(File.class), eq(JobYaml.class))).thenReturn(jobYaml);
        doNothing().when(stepExecutor).execute(any(JobStep.class));

        // Act
        jobExecutor.execute(job);

        // Assert
        verify(processRunner).run(any(RunCommand.class));
        verify(stepExecutor, times(2)).execute(any(JobStep.class)); // 2 steps in jobYaml
    }

    @Test
    void testExecute_WhenCloneFails_ShouldNotExecuteSteps() throws Exception {
        // Arrange
        Job job = createSimpleJob();
        ProcessResult failResult = createFailureResult("Clone failed");

        when(processRunner.run(any(RunCommand.class))).thenReturn(failResult);

        // Act
        jobExecutor.execute(job);

        // Assert
        verify(processRunner).run(any(RunCommand.class));
        verify(stepExecutor, never()).execute(any(JobStep.class));
        verify(objectMapper, never()).readValue(any(File.class), eq(JobYaml.class));
    }

    @Test
    void testExecute_WithNoSteps_ShouldCompleteWithoutError() throws Exception {
        // Arrange
        Job job = createSimpleJob();
        ProcessResult successResult = createSuccessResult("Cloned successfully");

        JobConfigYaml jobConfigYaml = new JobConfigYaml();
        jobConfigYaml.setSteps(Collections.emptyList());

        JobYaml emptyJobYaml = new JobYaml();
        emptyJobYaml.setJob(jobConfigYaml);

        when(processRunner.run(any(RunCommand.class))).thenReturn(successResult);
        when(objectMapper.readValue(any(File.class), eq(JobYaml.class))).thenReturn(emptyJobYaml);

        // Act
        jobExecutor.execute(job);

        // Assert
        verify(processRunner).run(any(RunCommand.class));
        verify(stepExecutor, never()).execute(any(JobStep.class));
    }

    @Test
    void testExecute_ShouldCloneWithCorrectRepository() throws Exception {
        // Arrange
        Job job = createSimpleJob();
        ProcessResult successResult = createSuccessResult("Cloned");
        JobYaml jobYaml = createJobYaml();

        when(processRunner.run(any(RunCommand.class))).thenReturn(successResult);
        when(objectMapper.readValue(any(File.class), eq(JobYaml.class))).thenReturn(jobYaml);

        // Act
        jobExecutor.execute(job);

        // Assert
        verify(processRunner).run(argThat(command -> command.getTool().equals("GIT") &&
                command.getArgs().contains("clone") &&
                command.getArgs().contains("https://github.com/test/repo.git")));
    }

    @Test
    void testExecute_WithMultiBranchJob_ShouldNotExecute() throws Exception {
        // Arrange - MultiBranch jobs are not handled in current implementation
        SimpleJobConfig config = new SimpleJobConfig(1L, "https://github.com/test/repo.git", null, "main",
                "knull.yaml");
        Job job = new Job(
                1L, "MultiBranch Job", "Description", JobType.MULTIBRANCH, config,
                false, true, null, new Date(), null, new Date());

        // Act
        jobExecutor.execute(job);

        // Assert - Nothing should happen for MULTIBRANCH jobs
        verify(processRunner, never()).run(any(RunCommand.class));
        verify(stepExecutor, never()).execute(any(JobStep.class));
    }

    private Job createSimpleJob() {
        SimpleJobConfig config = new SimpleJobConfig(
                1L,
                "https://github.com/test/repo.git",
                null,
                "main",
                "knull.yaml");

        return new Job(
                1L, "Test Job", "Description", JobType.SIMPLE, config,
                false, true, null, new Date(), null, new Date());
    }

    private JobYaml createJobYaml() {
        JobStep step1 = new JobStep();
        step1.setName("Step 1");

        JobStep step2 = new JobStep();
        step2.setName("Step 2");

        JobConfigYaml jobConfigYaml = new JobConfigYaml();
        jobConfigYaml.setSteps(Arrays.asList(step1, step2));

        JobYaml jobYaml = new JobYaml();
        jobYaml.setJob(jobConfigYaml);

        return jobYaml;
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
