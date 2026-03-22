package com.chimera.planner;

import com.chimera.model.AgentTask;
import com.chimera.streams.TaskQueuePublisher;
import org.springframework.stereotype.Component;

/**
 * Minimal Planner implementation that forwards tasks to the stream abstraction without policy logic.
 *
 * <p><b>SRS:</b> §3.1 Planner → {@code task_queue}.
 * <b>User stories:</b> US-016 (task emission scaffold).</p>
 */
@Component
public class ScaffoldPlanner implements Planner {

    /**
     * {@inheritDoc}
     */
    @Override
    public void enqueue(TaskQueuePublisher publisher, AgentTask task) {
        publisher.publish(task);
    }
}
