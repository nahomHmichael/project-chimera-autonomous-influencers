package com.chimera.model;

import java.util.UUID;

/**
 * Worker output DTO carried on Redis {@code review_queue} (Worker → Judge).
 *
 * <p><b>SRS:</b> §6.2 {@code AgentResult}, §6.1 OCC.
 * <b>User stories:</b> US-009 (confidence for HITL), US-012 ({@code stateVersion} at task start).</p>
 *
 * @param taskId           correlates to {@link AgentTask#taskId()}
 * @param workerId         executing worker instance
 * @param success          whether the Worker completed without fatal error
 * @param confidenceScore  0.0–1.0 for Judge routing (NFR 1.0)
 * @param payload          JSON string of skill-specific output
 * @param stateVersion     OCC version captured when the task started
 */
public record AgentResult(
        UUID taskId,
        UUID workerId,
        boolean success,
        float confidenceScore,
        String payload,
        int stateVersion
) {
}
