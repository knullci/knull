package org.knullci.knull.application.interfaces;

import org.knullci.knull.application.command.TriggerBuildCommand;
import org.knullci.knull.domain.model.Build;

/**
 * Handler for triggering manual builds.
 */
public interface TriggerBuildCommandHandler {

    /**
     * Triggers a build for the given job with the latest commit from the configured
     * branch.
     * 
     * @param command The trigger build command
     * @return The created build, or null if the build could not be triggered
     */
    Build handle(TriggerBuildCommand command);
}
