package org.knullci.knull.application.interfaces;

import org.knullci.knull.application.command.CancelBuildCommand;
import org.knullci.knull.application.dto.CancelBuildResult;

/**
 * Handler interface for cancelling builds.
 */
public interface CancelBuildCommandHandler {

    CancelBuildResult handle(CancelBuildCommand command);
}
