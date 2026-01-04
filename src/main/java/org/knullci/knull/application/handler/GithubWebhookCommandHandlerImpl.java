package org.knullci.knull.application.handler;

import lombok.SneakyThrows;
import org.knullci.knull.application.command.ExecuteBuildCommand;
import org.knullci.knull.application.command.GithubWebhookCommand;
import org.knullci.knull.application.dto.GithubWebhookResponseDto;
import org.knullci.knull.application.interfaces.ExecuteBuildCommandHandler;
import org.knullci.knull.application.interfaces.GithubWebhookCommandHandler;
import org.knullci.knull.domain.enums.JobType;
import org.knullci.knull.domain.model.SimpleJobConfig;
import org.knullci.knull.domain.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GithubWebhookCommandHandlerImpl implements GithubWebhookCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(GithubWebhookCommandHandlerImpl.class);

    private final JobRepository jobRepository;
    private final ExecuteBuildCommandHandler executeBuildCommandHandler;

    public GithubWebhookCommandHandlerImpl(JobRepository jobRepository,
            ExecuteBuildCommandHandler executeBuildCommandHandler) {
        this.jobRepository = jobRepository;
        this.executeBuildCommandHandler = executeBuildCommandHandler;
    }

    @Override
    @SneakyThrows
    public GithubWebhookResponseDto handle(GithubWebhookCommand command) {

        // check if the job available for the incoming repo
        String repoName = command.getGithubWebhook().getRepository().getName();
        var job = this.jobRepository.getJobByRepoName(repoName);
        if (job.isEmpty()) {
            logger.info("No job found for repo: {}, skipping pipeline", repoName);
            return null;
        }

        String currentBranch = command.getGithubWebhook().getRef().replace("refs/heads/", "");

        if (job.get().getJobType().equals(JobType.SIMPLE)
                && job.get().getJobConfig() instanceof SimpleJobConfig simpleJobConfig) {
            if (!simpleJobConfig.getBranch().contains(currentBranch)) {
                logger.info("Branch {} not in the job config, skipping pipeline", currentBranch);
                return null;
            }
        }

        // Trigger build execution asynchronously
        logger.info("Triggering build for job: {} from repository: {}", job.get().getName(), repoName);

        executeBuildCommandHandler.handle(new ExecuteBuildCommand(
                job.get(),
                command.getGithubWebhook().getHeadCommit().getId(),
                command.getGithubWebhook().getHeadCommit().getMessage(),
                currentBranch,
                command.getGithubWebhook().getRepository().getOwner().getName(),
                command.getGithubWebhook().getRepository().getName(),
                command.getGithubWebhook().getRepository().getHtmlUrl(),
                command.getGithubWebhook().getSender().getLogin()));

        return null;
    }
}
