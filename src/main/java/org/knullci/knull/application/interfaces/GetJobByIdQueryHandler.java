package org.knullci.knull.application.interfaces;

import org.knullci.knull.application.dto.JobDetailDto;
import org.knullci.knull.application.query.GetJobByIdQuery;

public interface GetJobByIdQueryHandler {
    JobDetailDto handle(GetJobByIdQuery query);
}
