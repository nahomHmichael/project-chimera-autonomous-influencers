package com.chimera.judge;

import com.chimera.model.TransactionRequest;
import com.chimera.model.TransactionReviewResult;

/**
 * Scaffold CFO Sub-Judge: no budget or §7.6 validation yet.
 *
 * <p><b>Spec:</b> {@code specs/technical.md} §7.2, §7.6 (V-6 over-budget → {@link com.chimera.commerce.BudgetExceededException}).
 * <b>User stories:</b> US-014 (daily cap), US-013.</p>
 *
 * <p>Contract tests expect {@link #review(TransactionRequest)} to enforce caps; this class throws until logic exists.</p>
 */
public final class DefaultCfoJudge implements CfoJudge {

    /**
     * {@inheritDoc}
     *
     * @throws UnsupportedOperationException until daily spend and §7.6 validation are implemented
     */
    @Override
    public TransactionReviewResult review(TransactionRequest request) {
        throw new UnsupportedOperationException(
                "Implement governed budget validation per specs/technical.md §7.6 V-6 and US-014 "
                        + "(throw BudgetExceededException when projected spend exceeds cap)");
    }

    /**
     * Factory for tests and future DI.
     *
     * @return new instance
     */
    public static DefaultCfoJudge create() {
        return new DefaultCfoJudge();
    }
}
