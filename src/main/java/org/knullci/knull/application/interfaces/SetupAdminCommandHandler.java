package org.knullci.knull.application.interfaces;

import org.knullci.knull.application.command.SetupAdminCommand;
import org.knullci.knull.application.dto.SetupResult;

/**
 * Handler interface for first-time admin setup.
 */
public interface SetupAdminCommandHandler {

    /**
     * Execute the admin setup command.
     * 
     * @param command the setup command containing admin user details
     * @return SetupResult indicating success or failure with errors
     */
    SetupResult handle(SetupAdminCommand command);

    /**
     * Check if the application requires initial setup.
     * 
     * @return true if no users exist in the database
     */
    boolean isSetupRequired();
}
