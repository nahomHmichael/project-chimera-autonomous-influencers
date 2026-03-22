package com.chimera.model;

/**
 * Lifecycle state of a governed financial request after CFO Sub-Judge evaluation or MCP execution.
 *
 * <p><b>Spec:</b> {@code specs/technical.md} §7.5 ({@code approval_status}).
 * <b>User stories:</b> US-014 (reject / cap), US-013 (no execution while pending).</p>
 */
public enum ApprovalStatus {

    /** Default at creation; CFO has not decided; no financial execution. */
    PENDING,

    /** CFO passed policy; runtime may invoke governed MCP financial tools once per idempotency rules. */
    APPROVED,

    /** CFO denied or validation failed; no execution. */
    REJECTED,

    /** MCP / ledger reported success after {@link #APPROVED}. */
    EXECUTED,

    /** Approved path failed at MCP or chain; retry uses a new {@code transaction_id} per §7.3 BR-1. */
    FAILED
}
