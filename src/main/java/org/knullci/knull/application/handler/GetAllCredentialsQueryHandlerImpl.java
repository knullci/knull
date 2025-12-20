package org.knullci.knull.application.handler;

import org.knullci.knull.application.dto.CredentialDto;
import org.knullci.knull.application.factory.CredentialFactory;
import org.knullci.knull.application.interfaces.GetAllCredentialsQueryHandler;
import org.knullci.knull.application.query.GetAllCredentialsQuery;
import org.knullci.knull.domain.repository.CredentialRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetAllCredentialsQueryHandlerImpl implements GetAllCredentialsQueryHandler {

    private static final Logger logger = LoggerFactory.getLogger(GetAllCredentialsQueryHandlerImpl.class);

    private final CredentialRepository credentialRepository;

    public GetAllCredentialsQueryHandlerImpl(CredentialRepository credentialRepository) {
        this.credentialRepository = credentialRepository;
    }

    @Override
    public List<CredentialDto> handle(GetAllCredentialsQuery query) {
        logger.info("Fetching all credentials");
        var credentials = credentialRepository.findAll();
        return CredentialFactory.toDto(credentials);
    }
}
