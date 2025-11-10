package org.knullci.knull.persistence.repository;

import org.knullci.knull.domain.model.User;
import org.knullci.knull.domain.repository.UserRepository;
import org.knullci.knull.persistence.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final static Logger logger = LoggerFactory.getLogger(UserRepositoryImpl.class);

    private final JpaUserRepository jpaUserRepository;

    public UserRepositoryImpl(JpaUserRepository jpaUserRepository) {
        this.jpaUserRepository = jpaUserRepository;
    }

    @Override
    public Optional<User> findByUsername(String username) {

        logger.info("Finding user by username: {}", username);

        return this.jpaUserRepository.findByUsername(username)
                .map(user -> {
                    logger.info("User found for username: {}", username);
                    return UserMapper.fromEntity(user);
                })
                .or(() -> {
                    logger.warn("User not found for username: {}", username);
                    return Optional.empty();
                });
    }
}
