package com.chimera.streams;

import com.chimera.model.AgentTask;

/**
 * Abstraction for appending planned work to the Redis {@code task_queue} stream.
 *
 * <p><b>SRS:</b> §6.2 Schema 1 task lifecycle, Redis §4.
 * <b>User stories:</b> US-016 (Planner decomposes goals into tasks).</p>
 */
public interface TaskQueuePublisher {

    /**
     * Serializes and appends a task to {@link RedisStreamNames#TASK_QUEUE}.
     *
     * @param task immutable task DTO
     */
    void publish(AgentTask task);
}
