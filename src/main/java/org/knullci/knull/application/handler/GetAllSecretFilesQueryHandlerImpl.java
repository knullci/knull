package org.knullci.knull.application.handler;

import org.knullci.knull.application.dto.SecretFileDto;
import org.knullci.knull.application.interfaces.GetAllSecretFilesQueryHandler;
import org.knullci.knull.application.query.GetAllSecretFilesQuery;
import org.knullci.knull.domain.model.SecretFile;
import org.knullci.knull.domain.repository.SecretFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GetAllSecretFilesQueryHandlerImpl implements GetAllSecretFilesQueryHandler {

    private static final Logger logger = LoggerFactory.getLogger(GetAllSecretFilesQueryHandlerImpl.class);

    private final SecretFileRepository secretFileRepository;

    public GetAllSecretFilesQueryHandlerImpl(SecretFileRepository secretFileRepository) {
        this.secretFileRepository = secretFileRepository;
    }

    @Override
    public List<SecretFileDto> handle(GetAllSecretFilesQuery query) {
        logger.info("Fetching all secret files");
        return secretFileRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
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
