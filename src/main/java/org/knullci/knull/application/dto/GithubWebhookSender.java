package org.knullci.knull.application.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GithubWebhookSender {
    private String login;
    private String type;
}
