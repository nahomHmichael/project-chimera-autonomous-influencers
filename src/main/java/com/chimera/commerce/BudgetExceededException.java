package com.chimera.commerce;

/**
 * Raised when CFO Sub-Judge rejects a transaction because daily or policy caps would be exceeded.
 *
 * <p><b>SRS:</b> FR 5.2.
 * <b>User stories:</b> US-014 (max daily spend, default $50 USDC narrative).</p>
 */
public class BudgetExceededException extends RuntimeException {

    /**
     * @param message human-readable rejection (no wallet secrets)
     */
    public BudgetExceededException(String message) {
        super(message);
    }

    /**
     * @param message human-readable rejection
     * @param cause   underlying cause
     */
    public BudgetExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
