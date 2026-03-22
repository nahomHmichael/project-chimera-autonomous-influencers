package com.chimera.model;

import java.util.UUID;

/**
 * Commerce motion submitted to the CFO Sub-Judge before MCP wallet tools run.
 *
 * <p><b>SRS:</b> §6.2 commerce DTO, §4.5 agentic commerce.
 * <b>User stories:</b> US-013 (balance awareness), US-014 (daily cap), US-012 ({@code stateVersion}).</p>
 *
 * @param agentId      acting agent
 * @param toAddress    destination wallet / contract address string
 * @param amountUsdc   spend amount in USDC
 * @param reason       audit trail reason (e.g. pay for generation)
 * @param stateVersion OCC version at submission time
 */
public record TransactionRequest(
        UUID agentId,
        String toAddress,
        float amountUsdc,
        String reason,
        int stateVersion
) {
}
