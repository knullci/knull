package org.knullci.knull.application.interfaces;

import org.knullci.knull.application.dto.BuildDto;
import org.knullci.knull.application.query.GetBuildsByJobIdQuery;

import java.util.List;

public interface GetBuildsByJobIdQueryHandler {
    
    List<BuildDto> handle(GetBuildsByJobIdQuery query);
    
}
