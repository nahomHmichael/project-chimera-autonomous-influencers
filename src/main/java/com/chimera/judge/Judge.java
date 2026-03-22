package com.chimera.judge;

import com.chimera.model.AgentResult;
import com.chimera.model.GlobalStateSnapshot;
import com.chimera.model.ReviewDecision;

/**
 * Evaluates {@link AgentResult} payloads from {@code review_queue}, applies OCC, and emits routing.
 *
 * <p><b>SRS:</b> §3.1 Judge service, §6.1 OCC, §5.1 HITL.
 * <b>User stories:</b> US-010, US-011, US-012, US-017 (moderator trace).</p>
 */
public interface Judge {

    /**
     * Computes routing and whether a state commit may proceed at the current version.
     *
     * @param result partial or final Worker output
     * @param state  authoritative {@code state_version} snapshot for OCC
     * @return immutable review decision for orchestration and HITL APIs
     */
    ReviewDecision evaluate(AgentResult result, GlobalStateSnapshot state);
}
