package org.knullci.knull.infrastructure.service;

import org.knullci.knull.infrastructure.knullpojo.v1.JobStep;

public interface StepExecutor {
    void
    execute(JobStep step);
}
