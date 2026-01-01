package org.knullci.knull.application.dto;

/**
 * DTO for cancel build result.
 */
public class CancelBuildResult {

    private final boolean success;
    private final String message;

    public CancelBuildResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static CancelBuildResult success(String message) {
        return new CancelBuildResult(true, message);
    }

    public static CancelBuildResult failure(String message) {
        return new CancelBuildResult(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
