package org.knullci.knull.web.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.knullci.knull.infrastructure.service.NecroswordExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExecutorStatusControllerTest {

    @Mock
    private NecroswordExecutor necroswordExecutor;

    @InjectMocks
    private ExecutorStatusController controller;

    @BeforeEach
    void setUp() {
        // Setup is done via annotations
    }

    @Test
    void testGetStatus_WhenHealthyAndIdle_ShouldReturnConnectedAndIdle() {
        // Arrange
        when(necroswordExecutor.isHealthy()).thenReturn(true);
        when(necroswordExecutor.getRunningProcessCount()).thenReturn(0);

        // Act
        ResponseEntity<ExecutorStatusController.ExecutorStatus> response = controller.getStatus();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ExecutorStatusController.ExecutorStatus status = response.getBody();
        assertNotNull(status);
        assertEquals("Necrosword", status.name());
        assertTrue(status.connected());
        assertEquals("Connected", status.connectionStatus());
        assertEquals(0, status.runningProcesses());
        assertEquals("Idle", status.activityStatus());
        assertTrue(status.timestamp() > 0);

        verify(necroswordExecutor).isHealthy();
        verify(necroswordExecutor).getRunningProcessCount();
    }

    @Test
    void testGetStatus_WhenHealthyAndBusy_ShouldReturnConnectedAndBusy() {
        // Arrange
        when(necroswordExecutor.isHealthy()).thenReturn(true);
        when(necroswordExecutor.getRunningProcessCount()).thenReturn(5);

        // Act
        ResponseEntity<ExecutorStatusController.ExecutorStatus> response = controller.getStatus();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ExecutorStatusController.ExecutorStatus status = response.getBody();
        assertNotNull(status);
        assertEquals("Necrosword", status.name());
        assertTrue(status.connected());
        assertEquals("Connected", status.connectionStatus());
        assertEquals(5, status.runningProcesses());
        assertEquals("Busy", status.activityStatus());

        verify(necroswordExecutor).isHealthy();
        verify(necroswordExecutor).getRunningProcessCount();
    }

    @Test
    void testGetStatus_WhenUnhealthy_ShouldReturnDisconnected() {
        // Arrange
        when(necroswordExecutor.isHealthy()).thenReturn(false);
        when(necroswordExecutor.getRunningProcessCount()).thenReturn(-1);

        // Act
        ResponseEntity<ExecutorStatusController.ExecutorStatus> response = controller.getStatus();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ExecutorStatusController.ExecutorStatus status = response.getBody();
        assertNotNull(status);
        assertEquals("Necrosword", status.name());
        assertFalse(status.connected());
        assertEquals("Disconnected", status.connectionStatus());
        assertEquals(-1, status.runningProcesses());
        assertEquals("Idle", status.activityStatus()); // -1 < 0, so it's idle

        verify(necroswordExecutor).isHealthy();
        verify(necroswordExecutor).getRunningProcessCount();
    }

    @Test
    void testGetStatus_WhenSingleProcessRunning_ShouldReturnBusy() {
        // Arrange
        when(necroswordExecutor.isHealthy()).thenReturn(true);
        when(necroswordExecutor.getRunningProcessCount()).thenReturn(1);

        // Act
        ResponseEntity<ExecutorStatusController.ExecutorStatus> response = controller.getStatus();

        // Assert
        ExecutorStatusController.ExecutorStatus status = response.getBody();
        assertNotNull(status);
        assertEquals(1, status.runningProcesses());
        assertEquals("Busy", status.activityStatus());
    }

    @Test
    void testGetStatus_ShouldAlwaysReturnNecroswordAsName() {
        // Arrange
        when(necroswordExecutor.isHealthy()).thenReturn(true);
        when(necroswordExecutor.getRunningProcessCount()).thenReturn(0);

        // Act
        ResponseEntity<ExecutorStatusController.ExecutorStatus> response = controller.getStatus();

        // Assert
        ExecutorStatusController.ExecutorStatus status = response.getBody();
        assertNotNull(status);
        assertEquals("Necrosword", status.name());
    }

    @Test
    void testGetStatus_TimestampShouldBeCurrentTime() {
        // Arrange
        when(necroswordExecutor.isHealthy()).thenReturn(true);
        when(necroswordExecutor.getRunningProcessCount()).thenReturn(0);

        long beforeCall = System.currentTimeMillis();

        // Act
        ResponseEntity<ExecutorStatusController.ExecutorStatus> response = controller.getStatus();

        long afterCall = System.currentTimeMillis();

        // Assert
        ExecutorStatusController.ExecutorStatus status = response.getBody();
        assertNotNull(status);
        assertTrue(status.timestamp() >= beforeCall);
        assertTrue(status.timestamp() <= afterCall);
    }
}
