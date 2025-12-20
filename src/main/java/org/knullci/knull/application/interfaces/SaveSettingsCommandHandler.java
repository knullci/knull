package org.knullci.knull.application.interfaces;

import org.knullci.knull.application.command.SaveSettingsCommand;

public interface SaveSettingsCommandHandler {
    
    void handle(SaveSettingsCommand command);
    
}
