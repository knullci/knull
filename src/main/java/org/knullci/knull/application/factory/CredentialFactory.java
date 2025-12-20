package org.knullci.knull.application.factory;

import org.knullci.knull.application.command.CreateCredentialCommand;
import org.knullci.knull.application.dto.CredentialDto;
import org.knullci.knull.domain.enums.CredentialType;
import org.knullci.knull.domain.model.Credentials;
import org.knullci.knull.domain.model.TokenCredential;
import org.knullci.knull.domain.model.UsernamePasswordCredential;

import java.util.Date;
import java.util.List;

public class CredentialFactory {
    public static Credentials fromCommand(CreateCredentialCommand command, String encryptedPassword, String encryptedToken) {
        UsernamePasswordCredential usernamePasswordCredential = null;
        TokenCredential tokenCredential = null;
        
        if (command.getCredentialType() == CredentialType.USERNAME_PASSWORD) {
            usernamePasswordCredential = new UsernamePasswordCredential(
                command.getUsername(),
                encryptedPassword
            );
        } else if (command.getCredentialType() == CredentialType.TOKEN) {
            tokenCredential = new TokenCredential(encryptedToken);
        }
        
        return new Credentials(
            null,
            command.getName(),
            command.getDescription(),
            command.getCredentialType(),
            usernamePasswordCredential,
            tokenCredential,
            null, // createdBy - should be set from current user context
            new Date(),
            null, // modifiedBy
            null
        );
    }
    
    public static CredentialDto toDto(Credentials credentials) {
        return new CredentialDto(
            credentials.getId(),
            credentials.getName(),
            credentials.getDescription(),
            credentials.getCredentialType()
        );
    }
    
    public static List<CredentialDto> toDto(List<Credentials> credentials) {
        return credentials.stream()
            .map(CredentialFactory::toDto)
            .toList();
    }
}
