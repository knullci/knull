package org.knullci.knull.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "job_configs")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class JobConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "git_repository")
    private String gitRepository;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credentials")
    private Credentials credentials;

}
