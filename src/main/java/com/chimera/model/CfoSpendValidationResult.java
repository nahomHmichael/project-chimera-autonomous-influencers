package com.chimera.model;

/**
 * Structured outcome of CFO validation (mirrors approve/reject bodies in {@code specs/technical.md} §2).
 *
 * <p><b>SRS:</b> FR 5.2, commerce API contracts.
 * <b>User stories:</b> US-014 (reject with reason), US-015 (no key material in payload).</p>
 *
 * @param approved               whether execution may proceed to MCP wallet tools
 * @param rejectionReason        machine-readable cause when not approved
 * @param dailySpendRemaining    headroom after approval, or current headroom on reject
 * @param transactionHash        populated only after successful on-chain submit (nullable)
 */
public record CfoSpendValidationResult(
        boolean approved,
        String rejectionReason,
        float dailySpendRemaining,
        String transactionHash
) {
}
