package org.knullci.knull.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GithubWebhookRequestDto {
    private String ref;

    private GithubWebhookRepository repository;

    @JsonProperty("head_commit")
    private GithubWebhookHeadCommit headCommit;
    
    private GithubWebhookSender sender;

}
