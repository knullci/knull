package org.knullci.knull.application.interfaces;

import org.knullci.knull.application.dto.SettingsDto;
import org.knullci.knull.application.query.GetSettingsQuery;

public interface GetSettingsQueryHandler {
    
    SettingsDto handle(GetSettingsQuery query);
    
}
