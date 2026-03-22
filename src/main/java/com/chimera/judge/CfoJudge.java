package com.chimera.judge;

import com.chimera.commerce.BudgetExceededException;
import com.chimera.model.CfoSpendValidationResult;
import com.chimera.model.DailySpendSnapshot;
import com.chimera.model.TransactionRequest;

/**
 * CFO Sub-Judge — validates spend requests before any financial MCP execution (e.g. Coinbase AgentKit).
 *
 * <p><b>SRS:</b> §4.5 agentic commerce, FR 5.2.
 * <b>User stories:</b> US-014 (daily cap), US-013 (balance pre-check at Worker), US-015 (secrets via env).</p>
 */
public interface CfoJudge {

    /**
     * Validates a proposed transaction against remaining daily budget and policy.
     *
     * @param request proposed ledger motion
     * @param spend   tracked spend for the agent (Redis / ledger projection)
     * @return structured approval outcome (never {@code null})
     * @throws BudgetExceededException if the request would exceed configured caps
     */
    CfoSpendValidationResult validate(TransactionRequest request, DailySpendSnapshot spend)
            throws BudgetExceededException;
}
