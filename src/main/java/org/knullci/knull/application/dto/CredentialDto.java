package org.knullci.knull.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.knullci.knull.domain.enums.CredentialType;

@Getter
@AllArgsConstructor
public class CredentialDto {
    private Long id;
    private String name;
    private String description;
    private CredentialType credentialType;
}
