package com.chimera.judge;

import com.chimera.model.TransactionRequest;
import com.chimera.model.TransactionReviewResult;

/**
 * CFO Sub-Judge — financial governance gate before any ledger mutation or MCP financial execution.
 *
 * <p><b>Spec:</b> {@code specs/technical.md} §7.2 (CFO Judge gate), §2 commerce; FR 4.0 MCP-only execution.
 * <b>Architecture:</b> {@code research/architecture_strategy.md} §3 (CFO Sub-Judge), §6 MCP boundary.
 * <b>User stories:</b> US-014 (daily cap), US-013 (balance awareness), US-015 (secrets not in DTOs).</p>
 *
 * <p>Implementations evaluate {@link TransactionRequest} against budget and policy; they MUST NOT call
 * third-party HTTP APIs directly—post-approval execution belongs to MCP-mediated runtime paths.</p>
 */
public interface CfoJudge {

    /**
     * Reviews a single governed transaction proposal. Scaffold: no default implementation; callers
     * supply a concrete judge (or test double).
     *
     * @param request immutable envelope (normally {@link com.chimera.model.ApprovalStatus#PENDING} on first
     *                submit)
     * @return non-null outcome with {@link TransactionReviewResult#approvalStatus()} and OCC hint
     *         {@link TransactionReviewResult#newStateVersion()}
     * @throws com.chimera.commerce.BudgetExceededException     if implementation uses exceptional control flow
     *                                                        for over-cap (§7.6 V-6; US-014)
     * @throws com.chimera.commerce.InvalidTransactionRequestException if §7.6 validation fails
     */
    TransactionReviewResult review(TransactionRequest request);
}
