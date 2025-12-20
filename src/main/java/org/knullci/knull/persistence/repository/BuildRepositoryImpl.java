package org.knullci.knull.persistence.repository;

import org.knullci.knull.domain.model.Build;
import org.knullci.knull.domain.repository.BuildRepository;
import org.knullci.knull.persistence.mapper.BuildMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class BuildRepositoryImpl implements BuildRepository {

    private static final Logger logger = LoggerFactory.getLogger(BuildRepositoryImpl.class);

    private final KnullRepository<org.knullci.knull.persistence.entity.Build> knullRepository;
    private static final String BUILD_STORAGE_LOCATION = "storage/builds";

    public BuildRepositoryImpl() {
        this.knullRepository = new JsonKnullRepository<>(
                BUILD_STORAGE_LOCATION,
                org.knullci.knull.persistence.entity.Build.class
        );
    }

    @Override
    public Build saveBuild(Build build) {
        var _build = BuildMapper.toEntity(build);
        _build.setId(this.knullRepository.getNextFileId());
        this.knullRepository.save(_build.getId().toString(), _build);
        logger.info("Saved new build with id: {}", _build.getId());
        return BuildMapper.fromEntity(_build);
    }

    @Override
    public Optional<Build> findById(Long id) {
        logger.info("Fetching build by id: {}", id);
        return Optional.ofNullable(this.knullRepository.getByFileName(id.toString() + ".json"))
                .map(BuildMapper::fromEntity);
    }

    @Override
    public List<Build> findByJobId(Long jobId) {
        logger.info("Fetching builds for job id: {}", jobId);
        return this.knullRepository.getAll()
                .stream()
                .filter(build -> build.getJobId().equals(jobId))
                .map(BuildMapper::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Build> findAll() {
        logger.info("Fetching all builds");
        return this.knullRepository.getAll()
                .stream()
                .map(BuildMapper::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void updateBuild(Build build) {
        var _build = BuildMapper.toEntity(build);
        this.knullRepository.save(_build.getId().toString(), _build);
        logger.info("Updated build with id: {}", _build.getId());
    }
}
