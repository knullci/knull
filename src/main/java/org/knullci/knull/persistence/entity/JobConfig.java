package org.knullci.knull.persistence.entity;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = MultiBranchJobConfig.class, name = "MULTI_BRANCH"),
        @JsonSubTypes.Type(value = SimpleJobConfig.class, name = "SimpleJobConfig")
})
public abstract class JobConfig {

    private Long id;

    private String gitRepository;

    private Credentials credentials;

}
