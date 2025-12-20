package org.knullci.knull.domain.repository;

import org.knullci.knull.domain.model.Build;

import java.util.List;
import java.util.Optional;

public interface BuildRepository {
    
    Build saveBuild(Build build);
    
    Optional<Build> findById(Long id);
    
    List<Build> findByJobId(Long jobId);
    
    List<Build> findAll();
    
    void updateBuild(Build build);
    
}
