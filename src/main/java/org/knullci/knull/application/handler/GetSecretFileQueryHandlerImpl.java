package org.knullci.knull.application.handler;

import org.knullci.knull.application.dto.SecretFileDto;
import org.knullci.knull.application.interfaces.GetSecretFileQueryHandler;
import org.knullci.knull.application.query.GetSecretFileQuery;
import org.knullci.knull.domain.model.SecretFile;
import org.knullci.knull.domain.repository.SecretFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GetSecretFileQueryHandlerImpl implements GetSecretFileQueryHandler {

    private static final Logger logger = LoggerFactory.getLogger(GetSecretFileQueryHandlerImpl.class);

    private final SecretFileRepository secretFileRepository;

    public GetSecretFileQueryHandlerImpl(SecretFileRepository secretFileRepository) {
        this.secretFileRepository = secretFileRepository;
    }

    @Override
    public SecretFileDto handle(GetSecretFileQuery query) {
        logger.info("Fetching secret file with id: {}", query.getId());
        return secretFileRepository.findById(query.getId())
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Secret file not found with id: " + query.getId()));
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
