package org.knullci.knull.application.interfaces;

import org.knullci.knull.application.dto.CredentialDto;
import org.knullci.knull.application.query.GetAllCredentialsQuery;

import java.util.List;

public interface GetAllCredentialsQueryHandler {
    List<CredentialDto> handle(GetAllCredentialsQuery query);
}
