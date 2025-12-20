package org.knullci.knull.infrastructure.service;

import org.knullci.knull.infrastructure.dto.ProcessResult;
import org.knullci.knull.infrastructure.knullpojo.v1.RunCommand;

import java.nio.file.Path;

public interface ProcessRunner {
    ProcessResult run(RunCommand command);
    ProcessResult run(RunCommand command, Path workingDirectory);
}
