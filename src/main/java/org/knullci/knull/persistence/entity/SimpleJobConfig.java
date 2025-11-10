package org.knullci.knull.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "simple_job_config")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SimpleJobConfig extends JobConfig {

    private String branch;

    @Column(name = "script_file_location")
    private String scriptFileLocation;

}
