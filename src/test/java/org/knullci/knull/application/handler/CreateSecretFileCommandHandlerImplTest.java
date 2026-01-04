package org.knullci.knull.application.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.knullci.knull.application.command.CreateSecretFileCommand;
import org.knullci.knull.domain.model.SecretFile;
import org.knullci.knull.domain.repository.SecretFileRepository;
import org.knullci.knull.infrastructure.service.EncryptionService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateSecretFileCommandHandlerImplTest {

    @Mock
    private SecretFileRepository secretFileRepository;

    @Mock
    private EncryptionService encryptionService;

    @InjectMocks
    private CreateSecretFileCommandHandlerImpl handler;

    private CreateSecretFileCommand command;

    @BeforeEach
    void setUp() {
        command = new CreateSecretFileCommand(
                "test-secret",
                "Test description",
                "FILE",
                "test-content");
    }

    @Test
    void testHandle_WithNewSecretFile_ShouldCreateSuccessfully() {
        // Arrange
        when(secretFileRepository.existsByName(anyString())).thenReturn(false);
        when(encryptionService.encrypt(anyString())).thenReturn("encrypted-content");
        when(secretFileRepository.save(any(SecretFile.class))).thenAnswer(invocation -> {
            SecretFile sf = invocation.getArgument(0);
            sf.setId(1L);
            return sf;
        });

        // Act
        handler.handle(command);

        // Assert
        verify(secretFileRepository).existsByName("test-secret");
        verify(encryptionService).encrypt("test-content");
        verify(secretFileRepository).save(any(SecretFile.class));
    }

    @Test
    void testHandle_WithExistingSecretFile_ShouldThrowException() {
        // Arrange
        when(secretFileRepository.existsByName("test-secret")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> handler.handle(command));

        assertEquals("Secret file with name 'test-secret' already exists", exception.getMessage());
        verify(secretFileRepository).existsByName("test-secret");
        verify(secretFileRepository, never()).save(any(SecretFile.class));
    }

    @Test
    void testHandle_ShouldSetCorrectValues() {
        // Arrange
        when(secretFileRepository.existsByName(anyString())).thenReturn(false);
        when(encryptionService.encrypt(anyString())).thenReturn("encrypted-test-content");

        SecretFile[] capturedSecretFile = new SecretFile[1];
        when(secretFileRepository.save(any(SecretFile.class))).thenAnswer(invocation -> {
            capturedSecretFile[0] = invocation.getArgument(0);
            capturedSecretFile[0].setId(1L);
            return capturedSecretFile[0];
        });

        // Act
        handler.handle(command);

        // Assert
        assertNotNull(capturedSecretFile[0]);
        assertEquals("test-secret", capturedSecretFile[0].getName());
        assertEquals("encrypted-test-content", capturedSecretFile[0].getEncryptedContent());
        assertEquals("Test description", capturedSecretFile[0].getDescription());
        assertNotNull(capturedSecretFile[0].getId());
    }

    @Test
    void testHandle_WithEmptyName_ShouldStillProcess() {
        // Arrange
        CreateSecretFileCommand emptyNameCommand = new CreateSecretFileCommand(
                "",
                "Test description",
                "FILE",
                "test-content");
        when(secretFileRepository.existsByName(anyString())).thenReturn(false);
        when(encryptionService.encrypt(anyString())).thenReturn("encrypted-content");
        when(secretFileRepository.save(any(SecretFile.class))).thenAnswer(invocation -> {
            SecretFile sf = invocation.getArgument(0);
            sf.setId(1L);
            return sf;
        });

        // Act
        handler.handle(emptyNameCommand);

        // Assert
        verify(secretFileRepository).existsByName("");
        verify(secretFileRepository).save(any(SecretFile.class));
    }

    @Test
    void testHandle_WithNullDescription_ShouldHandleGracefully() {
        // Arrange
        CreateSecretFileCommand nullDescCommand = new CreateSecretFileCommand(
                "test-secret",
                null,
                "FILE",
                "test-content");
        when(secretFileRepository.existsByName(anyString())).thenReturn(false);
        when(encryptionService.encrypt(anyString())).thenReturn("encrypted-content");
        when(secretFileRepository.save(any(SecretFile.class))).thenAnswer(invocation -> {
            SecretFile sf = invocation.getArgument(0);
            sf.setId(1L);
            return sf;
        });

        // Act
        handler.handle(nullDescCommand);

        // Assert
        verify(secretFileRepository).save(any(SecretFile.class));
    }
}
