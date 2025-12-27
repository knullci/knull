package org.knullci.knull.infrastructure.service;

import org.knullci.knull.domain.model.Build;
import org.knullci.knull.domain.model.Job;

public interface KnullExecutor {
    void executeBuild(Build build, Job job);
}
