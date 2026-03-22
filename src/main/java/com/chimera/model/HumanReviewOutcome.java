package com.chimera.model;

import java.util.UUID;

/**
 * Response body for {@code POST /api/judge/review} after OCC commit attempt.
 *
 * <p><b>SRS:</b> §6.2 judge review API.
 * <b>User stories:</b> US-012 (new {@code state_version} on success), US-017.</p>
 *
 * @param taskId            task id
 * @param committed         whether global state advanced
 * @param newStateVersion   version after successful commit
 */
public record HumanReviewOutcome(
        UUID taskId,
        boolean committed,
        int newStateVersion
) {
}
