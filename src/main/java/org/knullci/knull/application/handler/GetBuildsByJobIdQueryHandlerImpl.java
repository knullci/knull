package org.knullci.knull.application.handler;

import org.knullci.knull.application.dto.BuildDto;
import org.knullci.knull.application.interfaces.GetBuildsByJobIdQueryHandler;
import org.knullci.knull.application.query.GetBuildsByJobIdQuery;
import org.knullci.knull.domain.repository.BuildRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GetBuildsByJobIdQueryHandlerImpl implements GetBuildsByJobIdQueryHandler {

    private static final Logger logger = LoggerFactory.getLogger(GetBuildsByJobIdQueryHandlerImpl.class);

    private final BuildRepository buildRepository;

    public GetBuildsByJobIdQueryHandlerImpl(BuildRepository buildRepository) {
        this.buildRepository = buildRepository;
    }

    @Override
    public List<BuildDto> handle(GetBuildsByJobIdQuery query) {
        logger.info("Fetching builds for job id: {}", query.getJobId());

        return buildRepository.findByJobId(query.getJobId())
                .stream()
                .sorted((b1, b2) -> Long.compare(b2.getId(), b1.getId())) // Sort by ID descending (newest first)
                .map(build -> new BuildDto(
                        build.getId(),
                        build.getJobId(),
                        build.getJobName(),
                        build.getCommitSha(),
                        build.getCommitMessage(),
                        build.getBranch(),
                        build.getRepositoryUrl(),
                        build.getRepositoryOwner(),
                        build.getRepositoryName(),
                        build.getStatus(),
                        build.getBuildLog(),
                        build.getStartedAt(),
                        build.getCompletedAt(),
                        build.getDuration(),
                        build.getTriggeredBy()))
                .collect(Collectors.toList());
    }
}
