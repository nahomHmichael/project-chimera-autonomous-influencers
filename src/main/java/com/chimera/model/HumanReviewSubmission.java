package com.chimera.model;

import java.util.UUID;

/**
 * Request body for {@code POST /api/judge/review} (human moderator action).
 *
 * <p><b>SRS:</b> §6.2 judge review API.
 * <b>User stories:</b> US-017 (approve / reject within SLA).</p>
 *
 * @param taskId      task id
 * @param decision    {@code APPROVE} or {@code REJECT}
 * @param reviewerId  authenticated moderator id
 * @param notes       free-text audit notes
 */
public record HumanReviewSubmission(
        UUID taskId,
        String decision,
        String reviewerId,
        String notes
) {
}
