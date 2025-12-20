package org.knullci.knull.application.handler;

import org.knullci.knull.application.command.CreateCredentialCommand;
import org.knullci.knull.application.dto.CredentialDto;
import org.knullci.knull.application.factory.CredentialFactory;
import org.knullci.knull.application.interfaces.CreateCredentialCommandHandler;
import org.knullci.knull.domain.repository.CredentialRepository;
import org.knullci.knull.infrastructure.service.EncryptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CreateCredentialCommandHandlerImpl implements CreateCredentialCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(CreateCredentialCommandHandlerImpl.class);

    private final CredentialRepository credentialRepository;
    private final EncryptionService encryptionService;

    public CreateCredentialCommandHandlerImpl(CredentialRepository credentialRepository, 
                                              EncryptionService encryptionService) {
        this.credentialRepository = credentialRepository;
        this.encryptionService = encryptionService;
    }

    @Override
    public CredentialDto handle(CreateCredentialCommand command) {
        logger.info("Creating new credential with name: {}", command.getName());
        
        String encryptedPassword = null;
        String encryptedToken = null;
        
        if (command.getPassword() != null && !command.getPassword().isEmpty()) {
            encryptedPassword = encryptionService.encrypt(command.getPassword());
        }
        
        if (command.getToken() != null && !command.getToken().isEmpty()) {
            encryptedToken = encryptionService.encrypt(command.getToken());
        }
        
        var credential = CredentialFactory.fromCommand(command, encryptedPassword, encryptedToken);
        var savedCredential = credentialRepository.saveCredential(credential);
        
        return CredentialFactory.toDto(savedCredential);
    }
}
