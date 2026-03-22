package com.chimera.model;

import java.util.UUID;

/**
 * Judge output after evaluating a Worker result (routes auto / HITL / retry).
 *
 * <p><b>SRS:</b> §5.1 NFR 1.0–1.2, §6.1 OCC commit semantics.
 * <b>User stories:</b> US-010, US-011, US-017 (trace id for moderators).</p>
 *
 * @param taskId               task under review
 * @param routing              high-level orchestration branch
 * @param hitlReason           nullable machine reason (e.g. {@code medium_confidence})
 * @param judgeConfidence      post-processing score used for APIs
 * @param occCommitAllowed     whether {@code state_version} allows mutation
 * @param resultingStateVersion version after commit when applicable; otherwise echo current
 * @param decisionTraceId      correlates to HITL UI reasoning trace
 */
public record ReviewDecision(
        UUID taskId,
        JudgeRouting routing,
        String hitlReason,
        float judgeConfidence,
        boolean occCommitAllowed,
        int resultingStateVersion,
        String decisionTraceId
) {
}
