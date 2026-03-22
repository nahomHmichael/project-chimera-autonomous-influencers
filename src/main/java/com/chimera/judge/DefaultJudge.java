package com.chimera.judge;

import com.chimera.model.AgentResult;
import com.chimera.model.GlobalStateSnapshot;
import com.chimera.model.ReviewDecision;

/**
 * Default Judge service: confidence routing, sensitive-topic HITL, and OCC gate.
 *
 * <p><b>SRS:</b> §5.1 NFR 1.0–1.2, §6.1 OCC.
 * <b>User stories:</b> US-010 (thresholds), US-011 (sensitive topics), US-012 ({@code state_version}).</p>
 *
 * <p>Scaffold: {@link #evaluate(AgentResult, GlobalStateSnapshot)} throws until routing logic exists.</p>
 */
public final class DefaultJudge implements Judge {

    /**
     * {@inheritDoc}
     *
     * @throws UnsupportedOperationException until Judge evaluation is implemented
     */
    @Override
    public ReviewDecision evaluate(AgentResult result, GlobalStateSnapshot state) {
        throw new UnsupportedOperationException(
                "Implement routing (>0.90 auto, 0.70–0.90 HITL, <0.70 reject), sensitive-topic override, "
                        + "and OCC per US-010, US-011, US-012; see research/architecture_strategy.md §4");
    }

    /**
     * Scaffold factory for tests and future DI.
     *
     * @return new instance
     */
    public static DefaultJudge create() {
        return new DefaultJudge();
    }
}
