package org.knullci.knull.application.command;

/**
 * Command to cancel a running build.
 */
public class CancelBuildCommand {

    private final Long buildId;

    public CancelBuildCommand(Long buildId) {
        this.buildId = buildId;
    }

    public Long getBuildId() {
        return buildId;
    }
}
