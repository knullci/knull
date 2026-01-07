package org.knullci.knull.application.interfaces;

import org.knullci.knull.application.command.UpdateJobCommand;

public interface UpdateJobCommandHandler {
    void handle(UpdateJobCommand command);
}
