package org.knullci.knull.application.handler;

import org.knullci.knull.application.command.CreateUserCommand;
import org.knullci.knull.application.dto.UserDto;
import org.knullci.knull.domain.enums.Role;
import org.knullci.knull.domain.model.User;
import org.knullci.knull.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Handler for creating new users.
 */
@Service
public class CreateUserCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(CreateUserCommandHandler.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CreateUserCommandHandler(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserDto handle(CreateUserCommand command) {
        logger.info("Creating new user: {}", command.getUsername());

        // Check if username already exists
        if (userRepository.findByUsername(command.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + command.getUsername());
        }

        // Create user with encrypted password
        User user = new User(
                null,
                command.getUsername(),
                command.getEmail(),
                passwordEncoder.encode(command.getPassword()),
                command.getDisplayName() != null ? command.getDisplayName() : command.getUsername(),
                command.getRole() != null ? command.getRole() : Role.VIEWER,
                command.getAdditionalPermissions() != null ? command.getAdditionalPermissions() : Set.of(),
                true, // active
                false, // not locked
                LocalDateTime.now(),
                LocalDateTime.now(),
                null // never logged in
        );

        User savedUser = userRepository.save(user);
        logger.info("User created successfully: {}", savedUser.getUsername());

        return toDto(savedUser);
    }

    private UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getRole(),
                user.getAdditionalPermissions(),
                user.isActive(),
                user.isAccountLocked(),
                user.getCreatedAt(),
                user.getLastLoginAt());
    }
}
