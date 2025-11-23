package org.knullci.knull.persistence.repository;

import org.knullci.knull.domain.model.User;
import org.knullci.knull.domain.repository.UserRepository;
import org.knullci.knull.persistence.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final static Logger logger = LoggerFactory.getLogger(UserRepositoryImpl.class);

    private final KnullRepository<org.knullci.knull.persistence.entity.User> knullRepository;

    private final static String USER_STORAGE_LOCATION = "storage/users";

    public UserRepositoryImpl() {
        this.knullRepository = new JsonKnullRepository<>(
                USER_STORAGE_LOCATION,
                org.knullci.knull.persistence.entity.User.class
        );
    }

    @Override
    public Optional<User> findByUsername(String username) {

        logger.info("Finding user by username: {}", username);

        var result =  this.knullRepository.getAll()
                .stream()
                .filter(user -> Objects.equals(user.getUsername(), username))
                .toList();

        if (result.isEmpty()) {
            logger.warn("User not found for username: {}", username);
            return Optional.empty();
        } else {
            logger.info("User found for username: {}", username);
            return result.stream()
                    .map(UserMapper::fromEntity)
                    .findFirst();
        }
    }

    @Override
    public void save(User user) {

        logger.info("Creating new user with name: {}", user.getUsername());

        var _user = UserMapper.toEntity(user);
        _user.setId(this.knullRepository.getNextFileId());
        this.knullRepository.save(_user.getId().toString(), _user);
        logger.info("Created new user");
    }

    @Override
    public long count() {
        return this.knullRepository.getAll().size();
    }

}
