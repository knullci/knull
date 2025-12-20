package org.knullci.knull.persistence.repository;

import org.knullci.knull.domain.model.Credentials;
import org.knullci.knull.domain.repository.CredentialRepository;
import org.knullci.knull.persistence.mapper.CredentialsMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class CredentialRepositoryImpl implements CredentialRepository {

    private final static Logger logger = LoggerFactory.getLogger(CredentialRepositoryImpl.class);

    private final KnullRepository<org.knullci.knull.persistence.entity.Credentials> knullRepository;

    private final static String CREDENTIAL_STORAGE_LOCATION = "storage/credentials";

    public CredentialRepositoryImpl() {
        this.knullRepository = new JsonKnullRepository<>(
                CREDENTIAL_STORAGE_LOCATION,
                org.knullci.knull.persistence.entity.Credentials.class
        );
    }

    @Override
    public Credentials saveCredential(Credentials credentials) {
        var _credential = CredentialsMapper.toEntity(credentials);
        _credential.setId(this.knullRepository.getNextFileId());
        this.knullRepository.save(_credential.getId().toString(), _credential);
        logger.info("Saved new credential with id: {}", _credential.getId());
        return CredentialsMapper.fromEntity(_credential);
    }

    @Override
    public Optional<Credentials> findById(Long id) {
        logger.info("Fetching credential by id: {}", id);
        return Optional.ofNullable(this.knullRepository.getByFileName(id.toString() + ".json"))
                .map(CredentialsMapper::fromEntity);
    }

    @Override
    public List<Credentials> findAll() {
        logger.info("Fetching all credentials");
        return this.knullRepository.getAll()
                .stream()
                .map(CredentialsMapper::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        this.knullRepository.deleteByFileName(id.toString() + ".json");
        logger.info("Deleted credential with id: {}", id);
    }
}
