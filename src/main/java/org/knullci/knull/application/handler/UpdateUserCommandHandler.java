package org.knullci.knull.application.handler;

import org.knullci.knull.application.command.UpdateUserCommand;
import org.knullci.knull.application.dto.UserDto;
import org.knullci.knull.domain.model.User;
import org.knullci.knull.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Handler for updating existing users.
 */
@Service
public class UpdateUserCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(UpdateUserCommandHandler.class);

    private final UserRepository userRepository;

    public UpdateUserCommandHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserDto handle(UpdateUserCommand command) {
        logger.info("Updating user with ID: {}", command.getUserId());

        User existingUser = userRepository.findById(command.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + command.getUserId()));

        // Create updated user (immutable, so we create new instance)
        User updatedUser = new User(
                existingUser.getId(),
                existingUser.getUsername(), // Username cannot be changed
                command.getEmail() != null ? command.getEmail() : existingUser.getEmail(),
                existingUser.getPassword(), // Password not changed here
                command.getDisplayName() != null ? command.getDisplayName() : existingUser.getDisplayName(),
                command.getRole() != null ? command.getRole() : existingUser.getRole(),
                command.getAdditionalPermissions() != null ? command.getAdditionalPermissions()
                        : existingUser.getAdditionalPermissions(),
                command.isActive(),
                command.isAccountLocked(),
                existingUser.getCreatedAt(),
                LocalDateTime.now(),
                existingUser.getLastLoginAt());

        User savedUser = userRepository.save(updatedUser);
        logger.info("User updated successfully: {}", savedUser.getUsername());

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
