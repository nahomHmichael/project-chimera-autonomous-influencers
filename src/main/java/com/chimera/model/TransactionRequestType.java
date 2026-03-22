package com.chimera.model;

/**
 * Classifies which MCP tool family may execute after CFO approval.
 *
 * <p><b>Spec:</b> {@code specs/technical.md} §7.4 ({@code request_type}).
 * <b>Architecture:</b> {@code research/architecture_strategy.md} (CFO gate before Coinbase / MCP commerce).</p>
 */
public enum TransactionRequestType {

    ON_CHAIN_TRANSFER,

    MCP_BILLED_USAGE,

    INTERNAL_LEDGER_ADJUSTMENT
}
