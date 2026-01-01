package org.knullci.knull.application.handler;

import org.knullci.knull.application.command.ToggleUserLockCommand;
import org.knullci.knull.application.command.UpdateUserCommand;
import org.knullci.knull.application.interfaces.ToggleUserLockCommandHandler;
import org.knullci.knull.domain.model.User;
import org.knullci.knull.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Handler for toggling user lock status.
 */
@Service
public class ToggleUserLockCommandHandlerImpl implements ToggleUserLockCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(ToggleUserLockCommandHandlerImpl.class);

    private final UserRepository userRepository;
    private final UpdateUserCommandHandler updateUserCommandHandler;

    public ToggleUserLockCommandHandlerImpl(UserRepository userRepository,
            UpdateUserCommandHandler updateUserCommandHandler) {
        this.userRepository = userRepository;
        this.updateUserCommandHandler = updateUserCommandHandler;
    }

    @Override
    public boolean handle(ToggleUserLockCommand command) {
        Long userId = command.getUserId();
        logger.info("Toggling lock status for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        boolean newLockStatus = !user.isAccountLocked();

        UpdateUserCommand updateCommand = new UpdateUserCommand(
                userId,
                user.getEmail(),
                user.getDisplayName(),
                user.getRole(),
                user.getAdditionalPermissions(),
                user.isActive(),
                newLockStatus);

        updateUserCommandHandler.handle(updateCommand);

        logger.info("User {} lock status changed to: {}", userId, newLockStatus);
        return newLockStatus;
    }
}
