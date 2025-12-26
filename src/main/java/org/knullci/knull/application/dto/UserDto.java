package org.knullci.knull.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.knullci.knull.domain.enums.Permission;
import org.knullci.knull.domain.enums.Role;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for User data transfer.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String displayName;
    private Role role;
    private Set<Permission> additionalPermissions;
    private boolean active;
    private boolean accountLocked;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
}
