package org.knullci.knull.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SecretFileDto {

    private Long id;

    private String name;

    private String description;

    private String type;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Note: We don't expose the encrypted content in the DTO for security
}
