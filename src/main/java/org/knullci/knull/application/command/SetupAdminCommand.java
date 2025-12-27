package org.knullci.knull.application.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Command for creating the initial admin user during first-time setup.
 * This is only used when no users exist in the database.
 */
@Getter
@AllArgsConstructor
public class SetupAdminCommand {
    private final String username;
    private final String email;
    private final String password;
    private final String displayName;
}
