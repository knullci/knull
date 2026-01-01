package org.knullci.knull.application.command;

/**
 * Command to delete a user.
 */
public class DeleteUserCommand {

    private final Long userId;

    public DeleteUserCommand(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }
}
