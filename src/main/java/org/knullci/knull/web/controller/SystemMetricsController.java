package org.knullci.knull.web.controller;

import org.knullci.knull.infrastructure.service.SystemMetricsService;
import org.knullci.knull.infrastructure.service.SystemMetricsService.SystemMetrics;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for system metrics API.
 * Provides real-time CPU and memory usage data for dashboard charts.
 */
@RestController
@RequestMapping("/api/system")
public class SystemMetricsController {

    private final SystemMetricsService metricsService;

    public SystemMetricsController(SystemMetricsService metricsService) {
        this.metricsService = metricsService;
    }

    /**
     * Get current system metrics (CPU, RAM)
     * Called by dashboard JavaScript for real-time charts
     */
    @GetMapping("/metrics")
    public ResponseEntity<SystemMetrics> getMetrics() {
        return ResponseEntity.ok(metricsService.getMetrics());
    }
}
