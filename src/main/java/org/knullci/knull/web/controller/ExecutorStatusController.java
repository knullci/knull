package org.knullci.knull.web.controller;

import org.knullci.knull.infrastructure.service.NecroswordExecutor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for executor status API.
 * Provides real-time status of the Necrosword executor.
 */
@RestController
@RequestMapping("/api/executor")
public class ExecutorStatusController {

    private final NecroswordExecutor necroswordExecutor;

    public ExecutorStatusController(NecroswordExecutor necroswordExecutor) {
        this.necroswordExecutor = necroswordExecutor;
    }

    /**
     * Get current executor status
     * Called by dashboard JavaScript for real-time status updates
     */
    @GetMapping("/status")
    public ResponseEntity<ExecutorStatus> getStatus() {
        boolean healthy = necroswordExecutor.isHealthy();
        int runningProcesses = necroswordExecutor.getRunningProcessCount();

        ExecutorStatus status = new ExecutorStatus(
                "Necrosword",
                healthy,
                healthy ? "Connected" : "Disconnected",
                runningProcesses,
                runningProcesses > 0 ? "Busy" : "Idle",
                System.currentTimeMillis());

        return ResponseEntity.ok(status);
    }

    /**
     * Executor status DTO
     */
    public record ExecutorStatus(
            String name,
            boolean connected,
            String connectionStatus,
            int runningProcesses,
            String activityStatus,
            long timestamp) {
    }
}
