package com.chimera.commerce;

/**
 * Raised when a Worker attempts a cost-incurring path without adequate funds (pre-MCP spend).
 *
 * <p><b>SRS:</b> FR 5.1.
 * <b>User stories:</b> US-013 ({@code get_balance} gate).</p>
 */
public class InsufficientBalanceException extends RuntimeException {

    /**
     * @param message explanation for logs (no secrets)
     */
    public InsufficientBalanceException(String message) {
        super(message);
    }

    /**
     * @param message explanation for logs
     * @param cause   underlying cause
     */
    public InsufficientBalanceException(String message, Throwable cause) {
        super(message, cause);
    }
}
