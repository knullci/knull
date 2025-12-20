package org.knullci.knull.infrastructure.dto;

import java.time.Duration;
import java.time.Instant;

public record ProcessResult(
        boolean success,
        int exitCode,
        String output,
        String error,
        Instant startedAt,
        Instant finishedAt,
        Duration duration
) {}
