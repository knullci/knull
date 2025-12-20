package org.knullci.knull.infrastructure.service;

import org.knullci.knull.infrastructure.dto.ProcessResult;
import org.knullci.knull.infrastructure.knullpojo.v1.JobStep;
import org.springframework.stereotype.Service;

@Service
public class KnullStepExecutor implements StepExecutor {

    private final ProcessRunner processRunner;

    public KnullStepExecutor(ProcessRunner processRunner) {
        this.processRunner = processRunner;
    }

    @Override
    public void execute(JobStep step) {
        ProcessResult processResult = this.processRunner.run(step.getRun());

        if(!processResult.success()) {
            throw new RuntimeException("Failed to execute step " + step.getName());
        }
    }

}
