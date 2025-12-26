package org.knullci.knull.domain.enums;

/**
 * Fine-grained permissions for Knull CI/CD system.
 * These can be assigned to roles or directly to users.
 */
public enum Permission {
    // User management
    USER_READ,
    USER_CREATE,
    USER_UPDATE,
    USER_DELETE,

    // Job management
    JOB_READ,
    JOB_CREATE,
    JOB_UPDATE,
    JOB_DELETE,
    JOB_EXECUTE,

    // Build management
    BUILD_READ,
    BUILD_CANCEL,
    BUILD_DELETE,

    // Credential management
    CREDENTIAL_READ,
    CREDENTIAL_CREATE,
    CREDENTIAL_UPDATE,
    CREDENTIAL_DELETE,

    // Secret file management
    SECRET_READ,
    SECRET_CREATE,
    SECRET_UPDATE,
    SECRET_DELETE,

    // Settings management
    SETTINGS_READ,
    SETTINGS_UPDATE,

    // System administration
    SYSTEM_ADMIN
}
