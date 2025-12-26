package org.knullci.knull.application.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.knullci.knull.domain.enums.Permission;
import org.knullci.knull.domain.enums.Role;

import java.util.Set;

/**
 * Command for creating a new user.
 */
@Getter
@AllArgsConstructor
public class CreateUserCommand {
    private final String username;
    private final String email;
    private final String password;
    private final String displayName;
    private final Role role;
    private final Set<Permission> additionalPermissions;
}
