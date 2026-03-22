package com.chimera.judge;

/**
 * Raised when {@code state_version} on a Worker result no longer matches authoritative global state (OCC).
 *
 * <p><b>SRS:</b> §6.1 optimistic concurrency.
 * <b>User stories:</b> US-012 (reject ghost updates).</p>
 */
public class StateDriftException extends RuntimeException {

    /**
     * @param message human-readable drift explanation
     */
    public StateDriftException(String message) {
        super(message);
    }

    /**
     * @param message human-readable drift explanation
     * @param cause   underlying cause
     */
    public StateDriftException(String message, Throwable cause) {
        super(message, cause);
    }
}
