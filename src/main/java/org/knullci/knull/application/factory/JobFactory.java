package org.knullci.knull.application.factory;

import org.knullci.knull.application.command.CreateJobCommand;
import org.knullci.knull.application.dto.JobDetailDto;
import org.knullci.knull.application.dto.JobDto;
import org.knullci.knull.domain.enums.JobType;
import org.knullci.knull.domain.model.Credentials;
import org.knullci.knull.domain.model.Job;
import org.knullci.knull.domain.model.JobConfig;
import org.knullci.knull.domain.model.MultiBranchJobConfig;
import org.knullci.knull.domain.model.SimpleJobConfig;

import java.util.List;

public class JobFactory {
    public static Job fromCommand(CreateJobCommand command, Credentials credentials) {
        JobConfig jobConfig = null;
        
        if (command.getJobType() == JobType.SIMPLE) {
            jobConfig = new SimpleJobConfig(
                null,
                command.getGitRepository(),
                credentials,
                command.getBranch(),
                command.getScriptFileLocation()
            );
        } else if (command.getJobType() == JobType.MULTIBRANCH) {
            jobConfig = new MultiBranchJobConfig(
                null,
                command.getGitRepository(),
                credentials,
                command.getBranchPattern(),
                command.getScriptFileLocation()
            );
        }
        
        return new Job(
                null,
                command.getName(),
                command.getDescription(),
                command.getJobType(),
                jobConfig,
                null,
                null,
                null,
                null
        );
    }

    public static JobDto toDto(Job job) {
        return new JobDto(
                job.getId(),
                job.getName(),
                job.getDescription(),
                job.getJobType()
        );
    }

    public static List<JobDto> toDto(List<Job> jobs) {
        return jobs.stream().map(JobFactory::toDto).toList();
    }
    
    public static JobDetailDto toDetailDto(Job job) {
        String gitRepository = null;
        String credentialName = null;
        String branch = null;
        String branchPattern = null;
        String scriptFileLocation = null;
        
        if (job.getJobConfig() != null) {
            gitRepository = job.getJobConfig().getGitRepository();
            scriptFileLocation = job.getJobType() == JobType.SIMPLE
                    ? ((SimpleJobConfig) job.getJobConfig()).getScriptFileLocation()
                    : ((MultiBranchJobConfig) job.getJobConfig()).getScriptFileLocation();
            
            if (job.getJobConfig().getCredentials() != null) {
                credentialName = job.getJobConfig().getCredentials().getName();
            }
            
            if (job.getJobType() == JobType.SIMPLE) {
                branch = ((SimpleJobConfig) job.getJobConfig()).getBranch();
            } else if (job.getJobType() == JobType.MULTIBRANCH) {
                branchPattern = ((MultiBranchJobConfig) job.getJobConfig()).getBranchPattern();
            }
        }
        
        return new JobDetailDto(
                job.getId(),
                job.getName(),
                job.getDescription(),
                job.getJobType(),
                gitRepository,
                credentialName,
                branch,
                branchPattern,
                scriptFileLocation,
                job.getCreateAt()
        );
    }
}
