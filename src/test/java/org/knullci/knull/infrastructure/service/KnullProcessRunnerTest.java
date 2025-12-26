package org.knullci.knull.infrastructure.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.knullci.knull.infrastructure.dto.ProcessResult;
import org.knullci.knull.infrastructure.knullpojo.v1.RunCommand;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class KnullProcessRunnerTest {

    private KnullProcessRunner processRunner;

    @BeforeEach
    void setUp() {
        processRunner = new KnullProcessRunner();
    }

    @Test
    void testRun_WithGitVersion_ShouldSucceed() {
        // Arrange - GIT is an allowed tool
        RunCommand command = new RunCommand("GIT", Collections.singletonList("--version"));

        // Act
        ProcessResult result = processRunner.run(command);

        // Assert
        assertTrue(result.success());
        assertEquals(0, result.exitCode());
        assertTrue(result.output().contains("git version"));
    }

    @Test
    void testRun_WithDisallowedTool_ShouldThrowSecurityException() {
        // Arrange - echo is NOT an allowed tool
        RunCommand command = new RunCommand("echo", Collections.singletonList("hello"));

        // Act & Assert
        assertThrows(SecurityException.class, () -> processRunner.run(command));
    }

    @Test
    void testRun_WithWorkingDirectory_ShouldExecuteInDirectory() {
        // Arrange
        RunCommand command = new RunCommand("GIT", Arrays.asList("rev-parse", "--show-toplevel"));
        Path workDir = Paths.get(System.getProperty("user.dir"));

        // Act
        ProcessResult result = processRunner.run(command, workDir);

        // Assert
        assertTrue(result.success());
        assertNotNull(result.output());
    }

    @Test
    void testRun_WithMultipleGitArgs_ShouldPassAllArgs() {
        // Arrange
        RunCommand command = new RunCommand("GIT", Arrays.asList("config", "--list", "--global"));

        // Act
        ProcessResult result = processRunner.run(command);

        // Assert - Git config may succeed or fail depending on global config
        assertNotNull(result.output());
    }

    @Test
    void testRun_ShouldCaptureDuration() {
        // Arrange
        RunCommand command = new RunCommand("GIT", Collections.singletonList("--version"));

        // Act
        ProcessResult result = processRunner.run(command);

        // Assert
        assertNotNull(result.duration());
        assertNotNull(result.startedAt());
        assertNotNull(result.finishedAt());
    }

    @Test
    void testRun_WithMvnVersion_ShouldSucceed() {
        // Arrange - MVN is an allowed tool
        RunCommand command = new RunCommand("MVN", Collections.singletonList("--version"));

        // Act
        ProcessResult result = processRunner.run(command);

        // Assert - Maven should be available
        assertTrue(result.success());
        assertNotNull(result.output());
    }

    @Test
    void testRun_WithNpmVersion_ShouldSucceed() {
        // Arrange - NPM is an allowed tool
        RunCommand command = new RunCommand("NPM", Collections.singletonList("--version"));

        // Act
        ProcessResult result = processRunner.run(command);

        // Assert
        assertTrue(result.success());
        assertNotNull(result.output());
    }

    @Test
    void testRun_ToolNameIsCaseInsensitive() {
        // Arrange - Test that tool names are case insensitive
        RunCommand command = new RunCommand("git", Collections.singletonList("--version"));

        // Act
        ProcessResult result = processRunner.run(command);

        // Assert
        assertTrue(result.success());
    }
}
