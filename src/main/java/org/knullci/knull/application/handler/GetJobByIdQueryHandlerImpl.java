package org.knullci.knull.application.handler;

import org.knullci.knull.application.dto.JobDetailDto;
import org.knullci.knull.application.factory.JobFactory;
import org.knullci.knull.application.interfaces.GetJobByIdQueryHandler;
import org.knullci.knull.application.query.GetJobByIdQuery;
import org.knullci.knull.domain.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GetJobByIdQueryHandlerImpl implements GetJobByIdQueryHandler {

    private static final Logger logger = LoggerFactory.getLogger(GetJobByIdQueryHandlerImpl.class);

    private final JobRepository jobRepository;

    public GetJobByIdQueryHandlerImpl(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Override
    public JobDetailDto handle(GetJobByIdQuery query) {
        logger.info("Fetching job with id: {}", query.getId());
        var job = jobRepository.getJobId(query.getId())
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + query.getId()));
        return JobFactory.toDetailDto(job);
    }
}
