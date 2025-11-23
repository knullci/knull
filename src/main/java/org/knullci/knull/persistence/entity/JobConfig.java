package org.knullci.knull.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class JobConfig {

    private Long id;

    private String gitRepository;

    private Credentials credentials;

}
