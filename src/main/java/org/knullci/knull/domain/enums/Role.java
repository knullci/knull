package org.knullci.knull.domain.enums;

/**
 * Predefined roles in the Knull CI/CD system.
 * Each role has a hierarchy: ADMIN > DEVELOPER > VIEWER
 */
public enum Role {
    /**
     * Full administrative access - can manage users, jobs, credentials, settings
     */
    ADMIN,

    /**
     * Can create, edit, and execute jobs - cannot manage users or settings
     */
    DEVELOPER,

    /**
     * Read-only access - can view jobs, builds, logs
     */
    VIEWER
}
