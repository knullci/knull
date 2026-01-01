package org.knullci.knull.application.interfaces;

import org.knullci.knull.application.command.DeleteUserCommand;

/**
 * Handler interface for deleting users.
 */
public interface DeleteUserCommandHandler {

    void handle(DeleteUserCommand command);
}
