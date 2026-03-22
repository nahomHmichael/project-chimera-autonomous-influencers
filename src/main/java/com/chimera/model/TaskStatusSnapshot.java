package com.chimera.model;

import java.util.UUID;

/**
 * API projection for {@code GET /api/tasks/{taskId}/status} (technical spec §2).
 *
 * <p><b>SRS:</b> §6.2 HTTP contracts.
 * <b>User stories:</b> US-017 (reviewer visibility), US-010 (HITL flags).</p>
 *
 * @param taskId            task id
 * @param status            lifecycle status string
 * @param confidenceScore   latest known score
 * @param hitlRequired      whether async review is blocking publish
 * @param hitlReason        reason code when HITL is required
 * @param stateVersion      current OCC version
 */
public record TaskStatusSnapshot(
        UUID taskId,
        String status,
        float confidenceScore,
        boolean hitlRequired,
        String hitlReason,
        int stateVersion
) {
}
