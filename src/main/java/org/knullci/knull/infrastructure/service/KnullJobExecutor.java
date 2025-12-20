package org.knullci.knull.infrastructure.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.knullci.knull.domain.enums.JobType;
import org.knullci.knull.domain.model.Job;
import org.knullci.knull.domain.model.SimpleJobConfig;
import org.knullci.knull.infrastructure.dto.ProcessResult;
import org.knullci.knull.infrastructure.enums.Tool;
import org.knullci.knull.infrastructure.knullpojo.v1.JobYaml;
import org.knullci.knull.infrastructure.knullpojo.v1.RunCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
public class KnullJobExecutor implements JobExecutor {

    private static final Logger logger = LoggerFactory.getLogger(KnullJobExecutor.class);

    private final StepExecutor stepExecutor;
    private final ProcessRunner processRunner;
    private final ObjectMapper mapper;

    public KnullJobExecutor(StepExecutor stepExecutor, ProcessRunner processRunner, ObjectMapper objectMapper) {
        this.stepExecutor = stepExecutor;
        this.processRunner = processRunner;
        this.mapper = objectMapper;
    }

    @Override
    @SneakyThrows
    public void execute(Job job) {
        logger.info("Started executing job: {}", job.getId());

        if(job.getJobType().equals(JobType.SIMPLE)) {
            SimpleJobConfig jobConfig = (SimpleJobConfig) job.getJobConfig();

            ProcessResult cloneRepo = this.processRunner.run(
                    new RunCommand(Tool.GIT.toString(), List.of("clone", job.getJobConfig().getGitRepository()))
            );

            if (!cloneRepo.success()) {
                logger.error("Failed to clone the repo");
                return;
            }

            String fileLocation = jobConfig.getScriptFileLocation();
            JobYaml jobYaml = mapper.readValue(new File(fileLocation), JobYaml.class);

            for (var step : jobYaml.getJob().getSteps()) {
                this.stepExecutor.execute(step);
            }
        }
    }

}
