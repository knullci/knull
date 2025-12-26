package org.knullci.knull.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.knullci.knull.domain.enums.Permission;
import org.knullci.knull.domain.enums.Role;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * JPA Entity for User with RBAC support.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {

	private Long id;

	private String username;

	private String email;

	private String password;

	private String displayName;

	private Role role = Role.VIEWER;

	// Stored as comma-separated values in database
	private String additionalPermissions;

	private boolean active = true;

	private boolean accountLocked = false;

	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;

	private LocalDateTime lastLoginAt;

	/**
	 * Get additional permissions as Set
	 */
	public Set<Permission> getAdditionalPermissionsSet() {
		Set<Permission> permissions = new HashSet<>();
		if (additionalPermissions != null && !additionalPermissions.isEmpty()) {
			for (String perm : additionalPermissions.split(",")) {
				try {
					permissions.add(Permission.valueOf(perm.trim()));
				} catch (IllegalArgumentException ignored) {
					// Skip invalid permission
				}
			}
		}
		return permissions;
	}

	/**
	 * Set additional permissions from Set
	 */
	public void setAdditionalPermissionsSet(Set<Permission> permissions) {
		if (permissions == null || permissions.isEmpty()) {
			this.additionalPermissions = null;
		} else {
			this.additionalPermissions = permissions.stream()
					.map(Enum::name)
					.reduce((a, b) -> a + "," + b)
					.orElse(null);
		}
	}
}
