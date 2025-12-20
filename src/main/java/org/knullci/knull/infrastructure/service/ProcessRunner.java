package org.knullci.knull.infrastructure.service;

import org.knullci.knull.infrastructure.dto.ProcessResult;
import org.knullci.knull.infrastructure.knullpojo.v1.RunCommand;

public interface ProcessRunner {
    ProcessResult run(RunCommand command);
}
