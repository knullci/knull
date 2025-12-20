package org.knullci.knull.infrastructure.service;

import org.knullci.knull.domain.model.Job;

public interface JobExecutor {
    void execute(Job job);
}
