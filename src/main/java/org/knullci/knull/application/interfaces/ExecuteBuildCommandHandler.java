package org.knullci.knull.application.interfaces;

import org.knullci.knull.application.command.ExecuteBuildCommand;

public interface ExecuteBuildCommandHandler {
    
    void handle(ExecuteBuildCommand command);
    
}
