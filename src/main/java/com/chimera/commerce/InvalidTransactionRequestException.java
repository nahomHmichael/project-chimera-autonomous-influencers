package com.chimera.commerce;

/**
 * Raised when a {@link com.chimera.model.TransactionRequest} fails structural or policy validation before
 * or during CFO Sub-Judge evaluation ({@code specs/technical.md} §7.6).
 *
 * <p><b>Spec:</b> {@code specs/technical.md} §7.6 (V-1–V-4, V-7).
 * <b>User stories:</b> US-013, US-014 (reject invalid motions without execution).</p>
 */
public class InvalidTransactionRequestException extends RuntimeException {

    /**
     * @param message human-readable reason (no secrets)
     */
    public InvalidTransactionRequestException(String message) {
        super(message);
    }

    /**
     * @param message human-readable reason
     * @param cause   underlying cause
     */
    public InvalidTransactionRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
