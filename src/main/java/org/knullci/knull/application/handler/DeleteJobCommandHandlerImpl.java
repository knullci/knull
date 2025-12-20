package org.knullci.knull.application.handler;

import org.knullci.knull.application.command.DeleteJobCommand;
import org.knullci.knull.application.interfaces.DeleteJobCommandHandler;
import org.knullci.knull.domain.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DeleteJobCommandHandlerImpl implements DeleteJobCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(DeleteJobCommandHandlerImpl.class);

    private final JobRepository jobRepository;

    public DeleteJobCommandHandlerImpl(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Override
    public void handle(DeleteJobCommand command) {
        logger.info("Deleting job with id: {}", command.getId());
        jobRepository.deleteJobById(command.getId());
        logger.info("Job deleted successfully");
    }
}
