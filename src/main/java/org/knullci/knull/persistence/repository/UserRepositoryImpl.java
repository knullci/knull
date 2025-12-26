package org.knullci.knull.persistence.repository;

import org.knullci.knull.domain.model.User;
import org.knullci.knull.domain.repository.UserRepository;
import org.knullci.knull.persistence.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private static final Logger logger = LoggerFactory.getLogger(UserRepositoryImpl.class);

    private final KnullRepository<org.knullci.knull.persistence.entity.User> knullRepository;

    private static final String USER_STORAGE_LOCATION = "storage/users";

    public UserRepositoryImpl() {
        this.knullRepository = new JsonKnullRepository<>(
                USER_STORAGE_LOCATION,
                org.knullci.knull.persistence.entity.User.class);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        logger.info("Finding user by username: {}", username);

        return knullRepository.getAll()
                .stream()
                .filter(user -> Objects.equals(user.getUsername(), username))
                .map(UserMapper::fromEntity)
                .findFirst();
    }

    @Override
    public Optional<User> findById(Long id) {
        logger.info("Finding user by ID: {}", id);

        return knullRepository.getAll()
                .stream()
                .filter(user -> Objects.equals(user.getId(), id))
                .map(UserMapper::fromEntity)
                .findFirst();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        logger.info("Finding user by email: {}", email);

        return knullRepository.getAll()
                .stream()
                .filter(user -> Objects.equals(user.getEmail(), email))
                .map(UserMapper::fromEntity)
                .findFirst();
    }

    @Override
    public List<User> findAll() {
        logger.info("Finding all users");

        return knullRepository.getAll()
                .stream()
                .map(UserMapper::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public User save(User user) {
        logger.info("Saving user: {}", user.getUsername());

        var entity = UserMapper.toEntity(user);

        if (entity.getId() == null) {
            // New user - assign ID
            entity.setId(knullRepository.getNextFileId());
            entity.setCreatedAt(LocalDateTime.now());
        }
        entity.setUpdatedAt(LocalDateTime.now());

        knullRepository.save(entity.getId().toString(), entity);
        logger.info("User saved with ID: {}", entity.getId());

        return UserMapper.fromEntity(entity);
    }

    @Override
    public void deleteById(Long id) {
        logger.info("Deleting user with ID: {}", id);
        knullRepository.deleteByFileName(id.toString());
    }

    @Override
    public long count() {
        return knullRepository.getAll().size();
    }

    @Override
    public boolean existsByUsername(String username) {
        return knullRepository.getAll()
                .stream()
                .anyMatch(user -> Objects.equals(user.getUsername(), username));
    }

    @Override
    public void updateLastLoginTime(Long userId) {
        logger.info("Updating last login time for user ID: {}", userId);

        knullRepository.getAll()
                .stream()
                .filter(user -> Objects.equals(user.getId(), userId))
                .findFirst()
                .ifPresent(entity -> {
                    entity.setLastLoginAt(LocalDateTime.now());
                    knullRepository.save(entity.getId().toString(), entity);
                });
    }
}
