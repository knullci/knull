package org.knullci.knull.application.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.knullci.knull.domain.enums.Permission;
import org.knullci.knull.domain.enums.Role;

import java.util.Set;

/**
 * Command for updating an existing user.
 */
@Getter
@AllArgsConstructor
public class UpdateUserCommand {
    private final Long userId;
    private final String email;
    private final String displayName;
    private final Role role;
    private final Set<Permission> additionalPermissions;
    private final boolean active;
    private final boolean accountLocked;
}
