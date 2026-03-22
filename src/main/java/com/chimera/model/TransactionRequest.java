package com.chimera.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Governed financial request envelope submitted to the CFO Sub-Judge before any MCP wallet or billed
 * tool runs ({@code specs/technical.md} §7).
 *
 * <p><b>Spec:</b> {@code specs/technical.md} §7.4–§7.7; §1 DTO rules (record, no untyped maps).
 * <b>Architecture:</b> {@code research/architecture_strategy.md} (Planner/Worker proposes → CFO Judge → MCP only).
 * <b>User stories:</b> US-012 ({@code stateVersion}), US-013–US-015, US-014 (daily cap).</p>
 *
 * @param transactionId        idempotency key for this attempt (§7.3 BR-1)
 * @param agentId              incurring agent ({@code AGENT_PERSONA.id})
 * @param requestType          selects post-approval MCP family (§7.3 BR-3)
 * @param amount               strictly positive when validated by implementation (§7.6 V-1)
 * @param currency             ISO 4217 or deployment crypto code (§7.6 V-2)
 * @param budgetCategory       CFO policy line (§7.6 V-3)
 * @param purpose              audit justification (§7.6 V-4)
 * @param stateVersion         OCC snapshot at submission (US-012)
 * @param requestedAt          creation time (RFC 3339 instant)
 * @param approvalStatus       typically {@link ApprovalStatus#PENDING} on ingress (§7.6 V-5)
 * @param counterpartyAddress  optional; on-chain destination (§7.4 optional wire field)
 * @param correlationTaskId    optional link to {@code AgentTask.taskId}
 */
public record TransactionRequest(
        UUID transactionId,
        UUID agentId,
        TransactionRequestType requestType,
        BigDecimal amount,
        String currency,
        BudgetCategory budgetCategory,
        String purpose,
        int stateVersion,
        Instant requestedAt,
        ApprovalStatus approvalStatus,
        String counterpartyAddress,
        UUID correlationTaskId
) {
}
