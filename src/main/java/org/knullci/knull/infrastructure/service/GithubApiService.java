package org.knullci.knull.infrastructure.service;

import org.knullci.knull.infrastructure.dto.GithubBranchInfoDto;
import org.knullci.knull.infrastructure.dto.UpdateCommitStatusDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.Optional;

@Service
public class GithubApiService implements GithubService {

    private final Logger logger = LoggerFactory.getLogger(GithubApiService.class);

    private final WebClient webClient;

    public GithubApiService(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public void updateCommitStatus(UpdateCommitStatusDto updateCommitStatus) {
        try {
            this.webClient.post()
                    .uri("/repos/{owner}/{repo}/statuses/{sha}",
                            updateCommitStatus.getOwner(), updateCommitStatus.getRepo(),
                            updateCommitStatus.getCommitSha())
                    .bodyValue(Map.of(
                            "state", updateCommitStatus.getCommitState().toString().toLowerCase(),
                            "description", updateCommitStatus.getDescription(),
                            "context", updateCommitStatus.getContext(),
                            "target_url", updateCommitStatus.getTargetUrl()))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            logger.error("Failed to update commit state: {}", e.toString());
        }
    }

    @Override
    public Optional<GithubBranchInfoDto> getLatestCommit(String owner, String repo, String branch, String token) {
        logger.info("Fetching latest commit for {}/{} branch: {}", owner, repo, branch);

        // Try with "token" format first (classic PAT), then "Bearer" (fine-grained PAT)
        String[] authFormats = { "token " + token, "Bearer " + token };

        for (String authHeader : authFormats) {
            try {
                GithubBranchInfoDto branchInfo = WebClient.builder()
                        .baseUrl("https://api.github.com")
                        .defaultHeader("Authorization", authHeader)
                        .defaultHeader("Accept", "application/vnd.github.v3+json")
                        .defaultHeader("User-Agent", "KnullCI")
                        .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                        .build()
                        .get()
                        .uri("/repos/{owner}/{repo}/branches/{branch}", owner, repo, branch)
                        .retrieve()
                        .bodyToMono(GithubBranchInfoDto.class)
                        .block();

                if (branchInfo != null && branchInfo.getCommit() != null) {
                    logger.info("Found latest commit: {} - {}",
                            branchInfo.getCommit().getSha(),
                            branchInfo.getCommit().getCommit() != null ? branchInfo.getCommit().getCommit().getMessage()
                                    : "No message");
                    return Optional.of(branchInfo);
                }
            } catch (Exception e) {
                String errorMsg = e.getMessage();
                // If it's a 401, try the next auth format
                if (errorMsg != null && errorMsg.contains("401")) {
                    logger.debug("Auth format failed, trying next: {}",
                            authHeader.substring(0, Math.min(10, authHeader.length())));
                    continue;
                }
                // For other errors, log and return empty
                logger.error("Failed to fetch latest commit for {}/{} branch {}: {}", owner, repo, branch, errorMsg);
                return Optional.empty();
            }
        }

        logger.error("Failed to fetch latest commit for {}/{} branch {} with all auth formats", owner, repo, branch);
        return Optional.empty();
    }
}
