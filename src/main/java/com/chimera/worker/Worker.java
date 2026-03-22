package com.chimera.worker;

import com.chimera.model.AgentResult;
import com.chimera.model.AgentTask;

/**
 * Executes a popped {@link AgentTask} from {@code task_queue} and returns an {@link AgentResult}
 * for {@code review_queue}.
 *
 * <p><b>SRS:</b> §3.1 Worker roles, §6.2 {@code AgentResult}.
 * <b>User stories:</b> US-001, US-004–US-007 (role-specific workers), US-009 (confidence score).</p>
 */
public interface Worker {

    /**
     * Stable identifier for observability (e.g. {@code TrendFetcher}, {@code PlatformPublisher}).
     *
     * @return non-null role name
     */
    String workerRole();

    /**
     * Runs worker logic for the given task. Must not mutate the input task record.
     *
     * @param task work item from Planner
     * @return result carrying {@code confidenceScore} and OCC {@code stateVersion}
     */
    AgentResult execute(AgentTask task);
}
