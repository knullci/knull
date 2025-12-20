package org.knullci.knull.infrastructure.config;

import org.knullci.knull.domain.repository.CredentialRepository;
import org.knullci.knull.domain.repository.SettingsRepository;
import org.knullci.knull.infrastructure.service.EncryptionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GithubConfig {

    @Value("${github.pat.token:}")
    private String githubPat;

    @Value("${github.api.base-url}")
    private String baseUrl;

    private final CredentialRepository credentialRepository;
    private final SettingsRepository settingsRepository;
    private final EncryptionService encryptionService;

    public GithubConfig(CredentialRepository credentialRepository, SettingsRepository settingsRepository, EncryptionService encryptionService) {
        this.credentialRepository = credentialRepository;
        this.settingsRepository = settingsRepository;
        this.encryptionService = encryptionService;
    }

    @Bean
    public WebClient gitHub() {
        String token = getGithubToken();
        
        return WebClient.builder()
                .baseUrl(this.baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .defaultHeader(HttpHeaders.USER_AGENT, "knull-ci")
                .build();
    }

    private String getGithubToken() {
        // First try to get from settings
        try {
            var settings = this.settingsRepository.getSettings();
            if (settings.isPresent() && settings.get().getGithubCredentialId() != null) {
                var credential = this.credentialRepository.findById(settings.get().getGithubCredentialId());
                if (credential.isPresent()) {
                    var cred = credential.get();
                    // Return decrypted token if it has a token credential
                    if (cred.getTokenCredential() != null) {
                        String encryptedToken = cred.getTokenCredential().getEncryptedToken();
                        return this.encryptionService.decrypt(encryptedToken);
                    }
                }
            }
        } catch (Exception e) {
            // Fall back to properties
        }
        
        // Fall back to properties
        return this.githubPat != null && !this.githubPat.isEmpty() ? this.githubPat : "";
    }

}
