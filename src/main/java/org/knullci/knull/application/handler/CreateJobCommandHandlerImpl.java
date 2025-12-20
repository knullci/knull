package org.knullci.knull.application.handler;

import lombok.SneakyThrows;
import org.knullci.knull.application.command.CreateJobCommand;
import org.knullci.knull.application.dto.CreateJobResponseDto;
import org.knullci.knull.application.factory.JobFactory;
import org.knullci.knull.application.interfaces.CreateJobCommandHandler;
import org.knullci.knull.domain.model.Credentials;
import org.knullci.knull.domain.repository.CredentialRepository;
import org.knullci.knull.domain.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CreateJobCommandHandlerImpl implements CreateJobCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(CreateJobCommandHandlerImpl.class);

    private final JobRepository jobRepository;
    private final CredentialRepository credentialRepository;

    public CreateJobCommandHandlerImpl(JobRepository jobRepository, CredentialRepository credentialRepository) {
        this.jobRepository = jobRepository;
        this.credentialRepository = credentialRepository;
    }

    @SneakyThrows
    @Override
    public CreateJobResponseDto handle(CreateJobCommand command) {

        logger.info("Creating new Job with name: {}", command.getName());
        
        // Fetch credentials if provided
        Credentials credentials = null;
        if (command.getCredentialId() != null) {
            credentials = credentialRepository.findById(command.getCredentialId())
                    .orElseThrow(() -> new RuntimeException("Credential not found with id: " + command.getCredentialId()));
        }
        
        this.jobRepository.saveJob(JobFactory.fromCommand(command, credentials));

        return null;
    }
}
