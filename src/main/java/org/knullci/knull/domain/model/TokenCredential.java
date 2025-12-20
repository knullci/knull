package org.knullci.knull.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenCredential {
    private String encryptedToken;
}
