package com.chimera.commerce;

/** Thrown when a Worker proposes a transaction exceeding the CFO daily limit — SRS FR 5.2 */
public class BudgetExceededException extends RuntimeException {
    public BudgetExceededException(String message) {
        super(message);
    }
}
