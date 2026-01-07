package org.knullci.knull.application.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Command to trigger a manual build for a job.
 * This will fetch the latest commit from the configured branch and start a
 * build.
 */
@Getter
@AllArgsConstructor
public class TriggerBuildCommand {

    private Long jobId;

    private String triggeredBy;
}
