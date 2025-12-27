package org.knullci.knull.infrastructure.knullpojo.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JobYaml {

    // Root-level name (flat structure)
    private String name;

    // Root-level steps (flat structure: steps directly at root)
    private List<JobStep> steps;

    // Nested job config (nested structure: job.steps)
    private JobConfigYaml job;

    /**
     * Get the effective name, checking both flat and nested structures.
     */
    public String getEffectiveName() {
        if (name != null) {
            return name;
        }
        if (job != null && job.getName() != null) {
            return job.getName();
        }
        return null;
    }

    /**
     * Get the effective steps, checking both flat and nested structures.
     */
    public List<JobStep> getEffectiveSteps() {
        // Prefer root-level steps (flat structure)
        if (steps != null && !steps.isEmpty()) {
            return steps;
        }
        // Fall back to nested structure
        if (job != null && job.getSteps() != null) {
            return job.getSteps();
        }
        return null;
    }

}
