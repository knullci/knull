package org.knullci.knull.infrastructure.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for collecting system metrics (CPU, Memory, etc.)
 * Uses Java's ManagementFactory and native macOS commands for accurate metrics.
 */
@Service
public class SystemMetricsService {

    private static final Logger logger = LoggerFactory.getLogger(SystemMetricsService.class);

    private final OperatingSystemMXBean osBean;
    private final MemoryMXBean memoryBean;
    private final Runtime runtime;
    private final long totalPhysicalMemory;

    public SystemMetricsService() {
        this.osBean = ManagementFactory.getOperatingSystemMXBean();
        this.memoryBean = ManagementFactory.getMemoryMXBean();
        this.runtime = Runtime.getRuntime();
        this.totalPhysicalMemory = getTotalPhysicalMemory();
    }

    /**
     * Get current system metrics snapshot
     */
    public SystemMetrics getMetrics() {
        MacOSMemoryInfo macMemory = getMacOSMemoryInfo();

        return new SystemMetrics(
                getCpuUsage(),
                getHeapMemoryUsage(),
                getNonHeapMemoryUsage(),
                macMemory,
                getAvailableProcessors(),
                Instant.now().toEpochMilli());
    }

    /**
     * Get CPU usage as percentage (0-100)
     */
    public double getCpuUsage() {
        double load = osBean.getSystemLoadAverage();
        int processors = osBean.getAvailableProcessors();

        if (load < 0) {
            if (osBean instanceof com.sun.management.OperatingSystemMXBean sunOsBean) {
                return sunOsBean.getCpuLoad() * 100;
            }
            return 0;
        }

        return Math.min(100, (load / processors) * 100);
    }

    /**
     * Get JVM heap memory usage
     */
    public MemoryInfo getHeapMemoryUsage() {
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        return new MemoryInfo(
                heapUsage.getUsed(),
                heapUsage.getCommitted(),
                heapUsage.getMax(),
                calculatePercentage(heapUsage.getUsed(), heapUsage.getMax()));
    }

    /**
     * Get JVM non-heap memory usage
     */
    public MemoryInfo getNonHeapMemoryUsage() {
        MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
        long max = nonHeapUsage.getMax() > 0 ? nonHeapUsage.getMax() : nonHeapUsage.getCommitted();
        return new MemoryInfo(
                nonHeapUsage.getUsed(),
                nonHeapUsage.getCommitted(),
                max,
                calculatePercentage(nonHeapUsage.getUsed(), max));
    }

    /**
     * Get macOS-specific memory breakdown using vm_stat command.
     * Matches Activity Monitor's display of App Memory, Wired, Compressed, etc.
     */
    public MacOSMemoryInfo getMacOSMemoryInfo() {
        try {
            Map<String, Long> vmStats = parseVmStat();
            long pageSize = getPageSizeFromVmStat(vmStats);

            // Get page counts from vm_stat
            long freePages = vmStats.getOrDefault("Pages free", 0L);
            long wiredPages = vmStats.getOrDefault("Pages wired down", 0L);
            long fileBackedPages = vmStats.getOrDefault("File-backed pages", 0L);
            long anonymousPages = vmStats.getOrDefault("Anonymous pages", 0L);
            // "Pages occupied by compressor" is the ACTUAL memory used by the compressor
            long compressorPages = vmStats.getOrDefault("Pages occupied by compressor", 0L);

            // Convert to bytes
            long free = freePages * pageSize;
            long wired = wiredPages * pageSize;
            long fileBacked = fileBackedPages * pageSize;
            long anonymous = anonymousPages * pageSize;
            long compressed = compressorPages * pageSize;

            // App Memory = Anonymous pages (includes active anonymous + inactive anonymous)
            // This is how Activity Monitor calculates it roughly
            long appMemory = anonymous * pageSize / pageSize; // Just anonymous pages

            // Alternative: App Memory ≈ Active + Inactive - File-backed + Speculative -
            // Purgeable
            // But using anonymous pages is more accurate for "App Memory"

            // Cached Files = File-backed pages
            long cached = fileBacked;

            // Memory Used = App + Wired + Compressed (excludes cached as it can be freed)
            long memoryUsed = appMemory + wired + compressed;

            // Usage percentage (based on used memory vs physical, capped at 100%)
            double usagePercent = Math.min(100, calculatePercentage(memoryUsed, totalPhysicalMemory));

            return new MacOSMemoryInfo(
                    totalPhysicalMemory,
                    memoryUsed,
                    appMemory,
                    wired,
                    compressed,
                    cached,
                    free,
                    usagePercent);

        } catch (Exception e) {
            logger.warn("Failed to get macOS memory info, falling back to basic metrics", e);
            return getFallbackMacOSMemoryInfo();
        }
    }

    private MacOSMemoryInfo getFallbackMacOSMemoryInfo() {
        if (osBean instanceof com.sun.management.OperatingSystemMXBean sunOsBean) {
            long total = sunOsBean.getTotalMemorySize();
            long free = sunOsBean.getFreeMemorySize();
            long used = total - free;
            double percent = Math.min(100, calculatePercentage(used, total));
            return new MacOSMemoryInfo(total, used, used, 0, 0, 0, free, percent);
        }
        return new MacOSMemoryInfo(0, 0, 0, 0, 0, 0, 0, 0);
    }

    private Map<String, Long> parseVmStat() throws Exception {
        Map<String, Long> stats = new HashMap<>();

        ProcessBuilder pb = new ProcessBuilder("vm_stat");
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Parse "key: value." format
                if (line.contains(":")) {
                    String[] parts = line.split(":");
                    if (parts.length == 2) {
                        String key = parts[0].trim();
                        String valueStr = parts[1].trim().replace(".", "");
                        try {
                            long value = Long.parseLong(valueStr);
                            stats.put(key, value);
                        } catch (NumberFormatException ignored) {
                            // Skip non-numeric values (like the header)
                        }
                    }
                }
            }
        }

        process.waitFor();
        return stats;
    }

    /**
     * Extract page size from vm_stat header line
     */
    private long getPageSizeFromVmStat(Map<String, Long> stats) {
        // Default to 16384 for Apple Silicon, 4096 for Intel
        // The header line says "page size of XXXXX bytes" but we can't easily parse it
        // So we check the system architecture
        String arch = System.getProperty("os.arch");
        if ("aarch64".equals(arch) || arch.contains("arm")) {
            return 16384; // Apple Silicon uses 16KB pages
        }
        return 4096; // Intel uses 4KB pages
    }

    private long getTotalPhysicalMemory() {
        if (osBean instanceof com.sun.management.OperatingSystemMXBean sunOsBean) {
            return sunOsBean.getTotalMemorySize();
        }
        return runtime.maxMemory();
    }

    public int getAvailableProcessors() {
        return osBean.getAvailableProcessors();
    }

    private double calculatePercentage(long used, long max) {
        if (max <= 0)
            return 0;
        return (double) used / max * 100;
    }

    // Record classes for structured data
    public record SystemMetrics(
            double cpuUsage,
            MemoryInfo heapMemory,
            MemoryInfo nonHeapMemory,
            MacOSMemoryInfo systemMemory,
            int processors,
            long timestamp) {
    }

    public record MemoryInfo(
            long used,
            long committed,
            long max,
            double usagePercent) {
    }

    public record MacOSMemoryInfo(
            long physical,
            long used,
            long appMemory,
            long wired,
            long compressed,
            long cached,
            long free,
            double usagePercent) {

        public String formatBytes(long bytes) {
            if (bytes < 1024)
                return bytes + " B";
            if (bytes < 1024 * 1024)
                return String.format("%.1f KB", bytes / 1024.0);
            if (bytes < 1024 * 1024 * 1024)
                return String.format("%.1f MB", bytes / (1024.0 * 1024));
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }
}
