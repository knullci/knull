package org.knullci.knull.application.interfaces;

import org.knullci.knull.application.command.CreateCredentialCommand;
import org.knullci.knull.application.dto.CredentialDto;

public interface CreateCredentialCommandHandler {
    CredentialDto handle(CreateCredentialCommand command);
}
