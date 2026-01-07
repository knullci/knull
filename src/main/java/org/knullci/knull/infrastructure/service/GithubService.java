package org.knullci.knull.infrastructure.service;

import org.knullci.knull.infrastructure.dto.GithubBranchInfoDto;
import org.knullci.knull.infrastructure.dto.UpdateCommitStatusDto;

import java.util.Optional;

public interface GithubService {
    void updateCommitStatus(UpdateCommitStatusDto updateCommitStatus);

    /**
     * Fetches the latest commit information for a branch.
     * 
     * @param owner  Repository owner
     * @param repo   Repository name
     * @param branch Branch name
     * @param token  GitHub access token
     * @return Optional containing branch info with latest commit, or empty if not
     *         found
     */
    Optional<GithubBranchInfoDto> getLatestCommit(String owner, String repo, String branch, String token);
}
