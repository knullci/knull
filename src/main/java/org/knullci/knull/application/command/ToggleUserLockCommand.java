package org.knullci.knull.application.command;

/**
 * Command to toggle user lock status.
 */
public class ToggleUserLockCommand {

    private final Long userId;

    public ToggleUserLockCommand(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }
}
