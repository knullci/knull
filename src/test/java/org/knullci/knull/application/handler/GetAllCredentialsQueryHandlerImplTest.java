package org.knullci.knull.application.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.knullci.knull.application.dto.CredentialDto;
import org.knullci.knull.application.query.GetAllCredentialsQuery;
import org.knullci.knull.domain.model.Credentials;
import org.knullci.knull.domain.repository.CredentialRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAllCredentialsQueryHandlerImplTest {

    @Mock
    private CredentialRepository credentialRepository;

    @InjectMocks
    private GetAllCredentialsQueryHandlerImpl handler;

    @Test
    void testHandle_WithMultipleCredentials_ShouldReturnAllAsDto() {
        // Arrange
        GetAllCredentialsQuery query = new GetAllCredentialsQuery();

        Credentials cred1 = mock(Credentials.class);
        when(cred1.getId()).thenReturn(1L);
        when(cred1.getName()).thenReturn("Credential 1");

        Credentials cred2 = mock(Credentials.class);
        when(cred2.getId()).thenReturn(2L);
        when(cred2.getName()).thenReturn("Credential 2");

        when(credentialRepository.findAll()).thenReturn(Arrays.asList(cred1, cred2));

        // Act
        List<CredentialDto> result = handler.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(credentialRepository).findAll();
    }

    @Test
    void testHandle_WithEmptyRepository_ShouldReturnEmptyList() {
        // Arrange
        GetAllCredentialsQuery query = new GetAllCredentialsQuery();
        when(credentialRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<CredentialDto> result = handler.handle(query);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(credentialRepository).findAll();
    }

    @Test
    void testHandle_ShouldCallRepositoryOnce() {
        // Arrange
        GetAllCredentialsQuery query = new GetAllCredentialsQuery();
        when(credentialRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        handler.handle(query);

        // Assert
        verify(credentialRepository, times(1)).findAll();
    }
}
