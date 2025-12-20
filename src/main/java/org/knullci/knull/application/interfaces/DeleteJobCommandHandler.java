package org.knullci.knull.application.interfaces;

import org.knullci.knull.application.command.DeleteJobCommand;

public interface DeleteJobCommandHandler {
    void handle(DeleteJobCommand command);
}
