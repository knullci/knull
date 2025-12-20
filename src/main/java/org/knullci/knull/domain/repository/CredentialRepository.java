package org.knullci.knull.domain.repository;

import org.knullci.knull.domain.model.Credentials;

import java.util.List;
import java.util.Optional;

public interface CredentialRepository {
    Credentials saveCredential(Credentials credentials);
    
    Optional<Credentials> findById(Long id);
    
    List<Credentials> findAll();
    
    void deleteById(Long id);
}
