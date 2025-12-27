package org.knullci.knull.application.dto;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Result object for admin setup operation.
 */
@Getter
public class SetupResult {
    private final boolean success;
    private final String username;
    private final List<String> errors;

    private SetupResult(boolean success, String username, List<String> errors) {
        this.success = success;
        this.username = username;
        this.errors = errors != null ? errors : new ArrayList<>();
    }

    public static SetupResult success(String username) {
        return new SetupResult(true, username, null);
    }

    public static SetupResult failure(List<String> errors) {
        return new SetupResult(false, null, errors);
    }

    public static SetupResult failure(String error) {
        return new SetupResult(false, null, List.of(error));
    }

    public boolean hasErrors() {
        return !success && errors != null && !errors.isEmpty();
    }
}
