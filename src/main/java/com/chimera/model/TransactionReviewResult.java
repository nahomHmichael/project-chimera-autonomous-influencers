package com.chimera.model;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Immutable outcome of {@link com.chimera.judge.CfoJudge#review(TransactionRequest)} — CFO Sub-Judge
 * decision surface without embedding MCP or ledger side effects.
 *
 * <p><b>Spec:</b> {@code specs/technical.md} §7.2–§7.6 (CFO gate, {@code approval_status}, validation).
 * <b>User stories:</b> US-014 (budget outcome), US-012 ({@code newStateVersion} OCC bump on commit).</p>
 *
 * @param transactionId       echoes {@link TransactionRequest#transactionId()}
 * @param approvalStatus      resulting status after this review (e.g. {@link ApprovalStatus#APPROVED})
 * @param newStateVersion     authoritative global/spend snapshot version after a successful commit, or
 *                            prior version when unchanged (implementation-defined until wired to OCC)
 * @param rejectionReason     machine-readable cause when not {@link ApprovalStatus#APPROVED}; empty when
 *                            approved
 * @param dailySpendRemaining headroom after decision, for operator/API parity with §2 commerce responses
 */
public record TransactionReviewResult(
        UUID transactionId,
        ApprovalStatus approvalStatus,
        int newStateVersion,
        String rejectionReason,
        BigDecimal dailySpendRemaining
) {
}
