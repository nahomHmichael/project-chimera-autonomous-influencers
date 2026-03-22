package com.chimera.model;

/**
 * Outcome band for confidence-based routing and mandatory HITL overrides.
 *
 * <p><b>SRS:</b> §5.1 NFR 1.0–1.2.
 * <b>User stories:</b> US-010 (threshold routing), US-011 (sensitive topic → HITL).</p>
 */
public enum JudgeRouting {

    /** Score &gt; 0.90 and policy checks passed — allow auto execution path. */
    AUTO_EXECUTE,

    /** Score 0.70–0.90 or classifier uncertainty — async human review. */
    HITL_QUEUE,

    /** Score &lt; 0.70 or fatal validation — Planner retry / replan. */
    REJECT_RETRY
}
