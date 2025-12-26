package org.knullci.knull.domain.model;

import org.knullci.knull.domain.enums.Permission;
import org.knullci.knull.domain.enums.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Spring Security UserDetails implementation with RBAC support.
 */
public class KnullUserDetails implements UserDetails {

	private final User user;
	private final Set<GrantedAuthority> authorities;

	public KnullUserDetails(User user) {
		this.user = user;
		this.authorities = buildAuthorities(user);
	}

	private Set<GrantedAuthority> buildAuthorities(User user) {
		Set<GrantedAuthority> authorities = new HashSet<>();

		// Add role as authority
		if (user.getRole() != null) {
			authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

			// Add all permissions from role
			RolePermissions.getPermissions(user.getRole())
					.forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission.name())));
		}

		// Add additional permissions
		if (user.getAdditionalPermissions() != null) {
			user.getAdditionalPermissions()
					.forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission.name())));
		}

		return authorities;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		return user.getUsername();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return !user.isAccountLocked();
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return user.isActive();
	}

	/**
	 * Get the underlying User domain object
	 */
	public User getUser() {
		return user;
	}

	/**
	 * Check if user has a specific permission
	 */
	public boolean hasPermission(Permission permission) {
		return user.hasPermission(permission);
	}

	/**
	 * Check if user has a specific role
	 */
	public boolean hasRole(Role role) {
		return user.hasRole(role);
	}

	/**
	 * Get the user's role
	 */
	public Role getRole() {
		return user.getRole();
	}

	/**
	 * Get the user's display name
	 */
	public String getDisplayName() {
		return user.getDisplayName() != null ? user.getDisplayName() : user.getUsername();
	}

	/**
	 * Get all permissions as strings
	 */
	public Set<String> getPermissionStrings() {
		return authorities.stream()
				.map(GrantedAuthority::getAuthority)
				.filter(auth -> !auth.startsWith("ROLE_"))
				.collect(Collectors.toSet());
	}
}
