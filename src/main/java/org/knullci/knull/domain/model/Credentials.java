package org.knullci.knull.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.knullci.knull.domain.enums.CredentialType;

import java.util.Date;

@Getter
@AllArgsConstructor
public class Credentials {
    private Long id;

    private String name;

    private String description;

    private CredentialType credentialType;

    private User createdBy;

    private Date createAt;

    private User modifiedBy;

    private Date modifiedAt;
}
