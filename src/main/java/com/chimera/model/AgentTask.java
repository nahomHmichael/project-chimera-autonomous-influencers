package com.chimera.model;

import java.time.Instant;
import java.util.UUID;

/** Agent Task DTO — SRS §6.2 Schema 1 / US-009 */
public record AgentTask(
    UUID taskId,
    String taskType,
    String priority,
    TaskContext context,
    String assignedWorkerId,
    Instant createdAt,
    String status
) {}
