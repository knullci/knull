package org.knullci.knull.web.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.knullci.knull.application.command.CreateSecretFileCommand;
import org.knullci.knull.application.command.DeleteSecretFileCommand;
import org.knullci.knull.application.dto.SecretFileDto;
import org.knullci.knull.application.interfaces.CreateSecretFileCommandHandler;
import org.knullci.knull.application.interfaces.DeleteSecretFileCommandHandler;
import org.knullci.knull.application.interfaces.GetAllSecretFilesQueryHandler;
import org.knullci.knull.application.interfaces.GetSecretFileQueryHandler;
import org.knullci.knull.application.query.GetAllSecretFilesQuery;
import org.knullci.knull.application.query.GetSecretFileQuery;
import org.springframework.ui.Model;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecretFileControllerTest {

    @Mock
    private GetAllSecretFilesQueryHandler getAllSecretFilesQueryHandler;

    @Mock
    private GetSecretFileQueryHandler getSecretFileQueryHandler;

    @Mock
    private CreateSecretFileCommandHandler createSecretFileCommandHandler;

    @Mock
    private DeleteSecretFileCommandHandler deleteSecretFileCommandHandler;

    @Mock
    private Model model;

    @InjectMocks
    private SecretFileController controller;

    @BeforeEach
    void setUp() {
        // Setup common mocks if needed
    }

    @Test
    void testGetAllSecretFiles_ShouldReturnIndexView() {
        // Arrange
        List<SecretFileDto> secretFiles = Arrays.asList(
                createSecretFileDto(1L, "secret1", "Description 1"),
                createSecretFileDto(2L, "secret2", "Description 2"));
        when(getAllSecretFilesQueryHandler.handle(any(GetAllSecretFilesQuery.class)))
                .thenReturn(secretFiles);

        // Act
        String viewName = controller.listSecretFiles(model);

        // Assert
        assertEquals("secret-files/index", viewName);
        verify(model).addAttribute(eq("secretFiles"), eq(secretFiles));
        verify(getAllSecretFilesQueryHandler).handle(any(GetAllSecretFilesQuery.class));
    }

    @Test
    void testGetSecretFileById_WithValidId_ShouldReturnViewPage() {
        // Arrange
        Long secretId = 123L;
        SecretFileDto secretFileDto = new SecretFileDto();
        secretFileDto.setId(secretId);
        secretFileDto.setName("test-secret");
        secretFileDto.setDescription("test description");

        when(getSecretFileQueryHandler.handle(any(GetSecretFileQuery.class)))
                .thenReturn(secretFileDto);

        // Act
        String viewName = controller.viewSecretFile(secretId, model);

        // Assert
        assertEquals("secret-files/view", viewName);
        verify(model).addAttribute(eq("secretFile"), eq(secretFileDto));
        verify(getSecretFileQueryHandler).handle(any(GetSecretFileQuery.class));
    }

    @Test
    void testShowCreateForm_ShouldReturnCreateView() {
        // Act
        String viewName = controller.showCreateForm();

        // Assert
        assertEquals("secret-files/create", viewName);
    }

    @Test
    void testCreateSecretFile_WithValidCommand_ShouldRedirectToIndex() {
        // Arrange
        String name = "new-secret";
        String description = "New description";
        String type = "FILE";
        String content = "new-content";

        SecretFileDto mockDto = new SecretFileDto();
        mockDto.setId(1L);
        mockDto.setName(name);
        when(createSecretFileCommandHandler.handle(any(CreateSecretFileCommand.class))).thenReturn(mockDto);

        // Act
        String redirectUrl = controller.createSecretFile(name, description, type, content);

        // Assert
        assertEquals("redirect:/secret-files", redirectUrl);
        verify(createSecretFileCommandHandler).handle(any(CreateSecretFileCommand.class));
    }

    @Test
    void testDeleteSecretFile_WithValidId_ShouldRedirectToIndex() {
        // Arrange
        Long secretId = 456L;
        doNothing().when(deleteSecretFileCommandHandler).handle(any(DeleteSecretFileCommand.class));

        // Act
        String redirectUrl = controller.deleteSecretFile(secretId);

        // Assert
        assertEquals("redirect:/secret-files", redirectUrl);
        verify(deleteSecretFileCommandHandler).handle(any(DeleteSecretFileCommand.class));
    }

    @Test
    void testCreateSecretFile_WhenHandlerThrowsException_ShouldPropagateException() {
        // Arrange
        String name = "duplicate-secret";
        String description = "Test description";
        String type = "FILE";
        String content = "test-content";

        doThrow(new IllegalArgumentException("Secret already exists"))
                .when(createSecretFileCommandHandler).handle(any(CreateSecretFileCommand.class));

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> controller.createSecretFile(name, description, type, content));
    }

    private SecretFileDto createSecretFileDto(Long id, String name, String description) {
        SecretFileDto dto = new SecretFileDto();
        dto.setId(id);
        dto.setName(name);
        dto.setDescription(description);
        return dto;
    }
}
