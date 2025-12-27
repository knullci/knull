package org.knullci.knull.application.handler;

import lombok.RequiredArgsConstructor;
import org.knullci.knull.application.command.SetupAdminCommand;
import org.knullci.knull.application.dto.SetupResult;
import org.knullci.knull.application.interfaces.SetupAdminCommandHandler;
import org.knullci.knull.domain.enums.Role;
import org.knullci.knull.domain.model.User;
import org.knullci.knull.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Handler for creating the initial admin user during first-time setup.
 * This handler does NOT require authentication as it's used when no users
 * exist.
 */
@Service
@RequiredArgsConstructor
public class SetupAdminCommandHandlerImpl implements SetupAdminCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(SetupAdminCommandHandlerImpl.class);

    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MAX_USERNAME_LENGTH = 50;
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_DISPLAY_NAME_LENGTH = 100;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public SetupResult handle(SetupAdminCommand command) {
        logger.info("Processing admin setup request for username: {}", command.getUsername());

        // Verify setup is still required (no users exist)
        if (!isSetupRequired()) {
            logger.warn("Setup attempted but users already exist in the database");
            return SetupResult.failure("Setup is not available. Users already exist.");
        }

        // Validate the command
        List<String> errors = validate(command);
        if (!errors.isEmpty()) {
            logger.warn("Admin setup validation failed with {} errors", errors.size());
            return SetupResult.failure(errors);
        }

        // Create the admin user
        User adminUser = createAdminUser(command);
        User savedUser = userRepository.save(adminUser);

        logger.info("Admin user '{}' created successfully during initial setup", savedUser.getUsername());

        return SetupResult.success(savedUser.getUsername());
    }

    @Override
    public boolean isSetupRequired() {
        return userRepository.count() == 0;
    }

    private List<String> validate(SetupAdminCommand command) {
        List<String> errors = new ArrayList<>();

        // Username validation
        if (command.getUsername() == null || command.getUsername().trim().isEmpty()) {
            errors.add("Username is required");
        } else {
            String username = command.getUsername().trim();
            if (username.length() < MIN_USERNAME_LENGTH) {
                errors.add("Username must be at least " + MIN_USERNAME_LENGTH + " characters");
            } else if (username.length() > MAX_USERNAME_LENGTH) {
                errors.add("Username must not exceed " + MAX_USERNAME_LENGTH + " characters");
            } else if (userRepository.findByUsername(username).isPresent()) {
                errors.add("Username already exists");
            }
        }

        // Email validation (optional but must be valid if provided)
        if (command.getEmail() != null && !command.getEmail().trim().isEmpty()) {
            String email = command.getEmail().trim();
            if (!isValidEmail(email)) {
                errors.add("Invalid email format");
            } else if (userRepository.findByEmail(email).isPresent()) {
                errors.add("Email already exists");
            }
        }

        // Password validation
        if (command.getPassword() == null || command.getPassword().isEmpty()) {
            errors.add("Password is required");
        } else if (command.getPassword().length() < MIN_PASSWORD_LENGTH) {
            errors.add("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }

        // Display name validation (optional)
        if (command.getDisplayName() != null && command.getDisplayName().length() > MAX_DISPLAY_NAME_LENGTH) {
            errors.add("Display name must not exceed " + MAX_DISPLAY_NAME_LENGTH + " characters");
        }

        return errors;
    }

    private boolean isValidEmail(String email) {
        // Simple email validation
        return email != null && email.contains("@") && email.contains(".")
                && email.indexOf("@") > 0
                && email.lastIndexOf(".") > email.indexOf("@") + 1
                && email.lastIndexOf(".") < email.length() - 1;
    }

    private User createAdminUser(SetupAdminCommand command) {
        String username = command.getUsername().trim();
        String email = command.getEmail() != null && !command.getEmail().trim().isEmpty()
                ? command.getEmail().trim()
                : null;
        String displayName = command.getDisplayName() != null && !command.getDisplayName().trim().isEmpty()
                ? command.getDisplayName().trim()
                : username;

        return new User(
                null,
                username,
                email,
                passwordEncoder.encode(command.getPassword()),
                displayName,
                Role.ADMIN,
                Set.of(),
                true, // active
                false, // not locked
                LocalDateTime.now(),
                LocalDateTime.now(),
                null // never logged in
        );
    }
}
