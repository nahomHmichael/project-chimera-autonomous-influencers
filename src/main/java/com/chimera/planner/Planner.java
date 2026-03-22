package com.chimera.planner;

import com.chimera.model.AgentTask;
import com.chimera.streams.TaskQueuePublisher;

/**
 * Decomposes campaign goals into {@link AgentTask} units and publishes them to {@code task_queue}.
 *
 * <p><b>SRS:</b> §3.1 Planner service, §6.2 task schema.
 * <b>User stories:</b> US-002 (semantic filter policy), US-006 (tier / budget planning), US-016 (DAG visibility).</p>
 */
public interface Planner {

    /**
     * Pushes a single planned task toward Workers via the task stream.
     *
     * @param publisher stream gateway (Redis implementation supplied later)
     * @param task      fully populated task (status typically {@code pending})
     */
    void enqueue(TaskQueuePublisher publisher, AgentTask task);
}
