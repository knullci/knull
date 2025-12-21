package org.knullci.knull.application.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.knullci.knull.application.command.CreateCredentialCommand;
import org.knullci.knull.application.dto.CredentialDto;
import org.knullci.knull.domain.model.Credentials;
import org.knullci.knull.domain.repository.CredentialRepository;
import org.knullci.knull.infrastructure.service.EncryptionService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateCredentialCommandHandlerImplTest {

        @Mock
        private CredentialRepository credentialRepository;

        @Mock
        private EncryptionService encryptionService;

        @InjectMocks
        private CreateCredentialCommandHandlerImpl handler;

        @Test
        void testHandle_WithUsernameAndPassword_ShouldEncryptPassword() {
                // Arrange
                CreateCredentialCommand command = new CreateCredentialCommand(
                                "Test Credential",
                                "Test Description",
                                null, // CredentialType
                                "testuser",
                                "testpassword",
                                null);

                when(encryptionService.encrypt("testpassword")).thenReturn("encrypted-password");

                when(credentialRepository.saveCredential(any(Credentials.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                // Act
                CredentialDto result = handler.handle(command);

                // Assert
                assertNotNull(result);
                verify(encryptionService).encrypt("testpassword");
                verify(encryptionService, never()).encrypt(eq(null));
                verify(credentialRepository).saveCredential(any(Credentials.class));
        }

        @Test
        void testHandle_WithToken_ShouldEncryptToken() {
                // Arrange
                CreateCredentialCommand command = new CreateCredentialCommand(
                                "GitHub Token",
                                "Description",
                                null,
                                null,
                                null,
                                "github_token_123");

                when(encryptionService.encrypt("github_token_123")).thenReturn("encrypted-token");

                when(credentialRepository.saveCredential(any(Credentials.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                // Act
                CredentialDto result = handler.handle(command);

                // Assert
                assertNotNull(result);
                verify(encryptionService).encrypt("github_token_123");
                verify(credentialRepository).saveCredential(any(Credentials.class));
        }

        @Test
        void testHandle_WithBothPasswordAndToken_ShouldEncryptBoth() {
                // Arrange
                CreateCredentialCommand command = new CreateCredentialCommand(
                                "Full Credential",
                                "Desc",
                                null,
                                "user",
                                "password",
                                "token");

                when(encryptionService.encrypt("password")).thenReturn("encrypted-password");
                when(encryptionService.encrypt("token")).thenReturn("encrypted-token");

                when(credentialRepository.saveCredential(any(Credentials.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                // Act
                CredentialDto result = handler.handle(command);

                // Assert
                assertNotNull(result);
                verify(encryptionService).encrypt("password");
                verify(encryptionService).encrypt("token");
                verify(credentialRepository).saveCredential(any(Credentials.class));
        }

        @Test
        void testHandle_WithEmptyPassword_ShouldNotEncrypt() {
                // Arrange
                CreateCredentialCommand command = new CreateCredentialCommand(
                                "Test Credential",
                                "Desc",
                                null,
                                "testuser",
                                "",
                                null);

                when(credentialRepository.saveCredential(any(Credentials.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                // Act
                CredentialDto result = handler.handle(command);

                // Assert
                assertNotNull(result);
                verify(encryptionService, never()).encrypt(anyString());
                verify(credentialRepository).saveCredential(any(Credentials.class));
        }
}
