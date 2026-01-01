package org.knullci.knull.application.interfaces;

import org.knullci.knull.application.command.ToggleUserLockCommand;

/**
 * Handler interface for toggling user lock status.
 */
public interface ToggleUserLockCommandHandler {

    /**
     * Toggles the lock status of a user.
     * 
     * @param command the command containing the user ID
     * @return true if user is now locked, false if unlocked
     */
    boolean handle(ToggleUserLockCommand command);
}
