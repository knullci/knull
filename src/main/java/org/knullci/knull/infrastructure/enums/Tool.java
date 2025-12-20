package org.knullci.knull.infrastructure.enums;

import java.util.Arrays;

public enum Tool {
    GIT("git"),
    NPM("npm"),
    MVN("mvn"),
    DOCKER("docker"),
    KUBECTL("kubectl");

    private final String executable;

    Tool(String executable) {
        this.executable = executable;
    }

    public String executable() {
        return executable;
    }

    public static Tool from(String value) {
        return Arrays.stream(values())
                .filter(t -> t.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() ->
                        new SecurityException("Tool not allowed: " + value)
                );
    }
}
