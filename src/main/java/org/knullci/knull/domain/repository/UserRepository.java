package org.knullci.knull.domain.repository;

import org.knullci.knull.domain.model.User;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByUsername(String username);
    void save(User user);
    long count();
}
