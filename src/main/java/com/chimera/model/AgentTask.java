package com.chimera.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Core task DTO carried on Redis {@code task_queue} (Planner → Worker).
 *
 * <p><b>SRS:</b> §6.2 Schema 1.
 * <b>User stories:</b> US-016 (Planner DAG), US-007 (task types toward publish), US-008 (reply tasks).</p>
 *
 * @param taskId            unique task id
 * @param taskType          e.g. {@code generate_content}, {@code reply_comment}, {@code execute_transaction}
 * @param priority          {@code high}, {@code medium}, or {@code low}
 * @param context           goal and MCP resource URIs
 * @param assignedWorkerId  nullable until claimed
 * @param createdAt         creation instant
 * @param status            {@code pending}, {@code in_progress}, {@code review}, or {@code complete}
 */
public record AgentTask(
        UUID taskId,
        String taskType,
        String priority,
        TaskContext context,
        String assignedWorkerId,
        Instant createdAt,
        String status
) {
}
