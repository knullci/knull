package org.knullci.knull.application.handler;

import org.knullci.knull.application.command.DeleteUserCommand;
import org.knullci.knull.application.interfaces.DeleteUserCommandHandler;
import org.knullci.knull.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Handler for deleting users.
 */
@Service
public class DeleteUserCommandHandlerImpl implements DeleteUserCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(DeleteUserCommandHandlerImpl.class);

    private final UserRepository userRepository;

    public DeleteUserCommandHandlerImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void handle(DeleteUserCommand command) {
        Long userId = command.getUserId();
        logger.info("Deleting user with ID: {}", userId);

        userRepository.deleteById(userId);

        logger.info("User deleted successfully: {}", userId);
    }
}
