package org.knullci.knull.application.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.knullci.knull.application.dto.SecretFileDto;
import org.knullci.knull.application.query.GetSecretFileQuery;
import org.knullci.knull.domain.model.SecretFile;
import org.knullci.knull.domain.repository.SecretFileRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetSecretFileQueryHandlerImplTest {

    @Mock
    private SecretFileRepository secretFileRepository;

    @InjectMocks
    private GetSecretFileQueryHandlerImpl handler;

    @Test
    void testHandle_WithExistingSecretFile_ShouldReturnDto() {
        // Arrange
        Long id = 1L;
        SecretFile secretFile = createTestSecretFile(id);
        when(secretFileRepository.findById(id)).thenReturn(Optional.of(secretFile));

        // Act
        SecretFileDto result = handler.handle(new GetSecretFileQuery(id));

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Test Secret", result.getName());
        assertEquals("Test Description", result.getDescription());
        assertEquals("/mnt/secrets", result.getMountPath());
        verify(secretFileRepository).findById(id);
    }

    @Test
    void testHandle_WithNonExistingSecretFile_ShouldThrowException() {
        // Arrange
        Long id = 999L;
        when(secretFileRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> handler.handle(new GetSecretFileQuery(id)));
        assertEquals("Secret file not found with id: 999", exception.getMessage());
        verify(secretFileRepository).findById(id);
    }

    @Test
    void testHandle_ShouldMapAllFieldsCorrectly() {
        // Arrange
        Long id = 1L;
        LocalDateTime now = LocalDateTime.now();
        SecretFile secretFile = new SecretFile(
                id,
                "Test Secret",
                "Test Description",
                SecretFile.SecretType.FILE,
                "encrypted-content",
                "/mnt/secrets",
                now,
                now);
        when(secretFileRepository.findById(id)).thenReturn(Optional.of(secretFile));

        // Act
        SecretFileDto result = handler.handle(new GetSecretFileQuery(id));

        // Assert
        assertEquals(id, result.getId());
        assertEquals("Test Secret", result.getName());
        assertEquals("Test Description", result.getDescription());
        assertEquals("FILE", result.getType());
        assertEquals("/mnt/secrets", result.getMountPath());
        assertEquals(now, result.getCreatedAt());
        assertEquals(now, result.getUpdatedAt());
    }

    @Test
    void testHandle_ShouldCallRepositoryOnce() {
        // Arrange
        Long id = 1L;
        when(secretFileRepository.findById(id)).thenReturn(Optional.of(createTestSecretFile(id)));

        // Act
        handler.handle(new GetSecretFileQuery(id));

        // Assert
        verify(secretFileRepository, times(1)).findById(id);
    }

    private SecretFile createTestSecretFile(Long id) {
        return new SecretFile(
                id,
                "Test Secret",
                "Test Description",
                SecretFile.SecretType.FILE,
                "encrypted-content",
                "/mnt/secrets",
                LocalDateTime.now(),
                LocalDateTime.now());
    }
}
