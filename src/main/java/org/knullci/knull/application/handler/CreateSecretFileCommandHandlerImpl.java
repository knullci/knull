package org.knullci.knull.application.handler;

import org.knullci.knull.application.command.CreateSecretFileCommand;
import org.knullci.knull.application.dto.SecretFileDto;
import org.knullci.knull.application.interfaces.CreateSecretFileCommandHandler;
import org.knullci.knull.domain.model.SecretFile;
import org.knullci.knull.domain.repository.SecretFileRepository;
import org.knullci.knull.infrastructure.service.EncryptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CreateSecretFileCommandHandlerImpl implements CreateSecretFileCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(CreateSecretFileCommandHandlerImpl.class);

    private final SecretFileRepository secretFileRepository;
    private final EncryptionService encryptionService;

    public CreateSecretFileCommandHandlerImpl(
            SecretFileRepository secretFileRepository,
            EncryptionService encryptionService) {
        this.secretFileRepository = secretFileRepository;
        this.encryptionService = encryptionService;
    }

    @Override
    public SecretFileDto handle(CreateSecretFileCommand command) {
        logger.info("Creating secret file: {}", command.getName());

        // Check if secret file with same name already exists
        if (secretFileRepository.existsByName(command.getName())) {
            throw new IllegalArgumentException("Secret file with name '" + command.getName() + "' already exists");
        }

        // Encrypt the content
        String encryptedContent = encryptionService.encrypt(command.getContent());

        // Create the secret file
        SecretFile secretFile = new SecretFile();
        secretFile.setName(command.getName());
        secretFile.setDescription(command.getDescription());
        secretFile.setType(SecretFile.SecretType.valueOf(command.getType()));
        secretFile.setEncryptedContent(encryptedContent);
        secretFile.setCreatedAt(LocalDateTime.now());
        secretFile.setUpdatedAt(LocalDateTime.now());

        // Save
        SecretFile saved = secretFileRepository.save(secretFile);

        // Convert to DTO
        return toDto(saved);
    }

    private SecretFileDto toDto(SecretFile secretFile) {
        SecretFileDto dto = new SecretFileDto();
        dto.setId(secretFile.getId());
        dto.setName(secretFile.getName());
        dto.setDescription(secretFile.getDescription());
        dto.setType(secretFile.getType().name());
        dto.setCreatedAt(secretFile.getCreatedAt());
        dto.setUpdatedAt(secretFile.getUpdatedAt());
        return dto;
    }
}
