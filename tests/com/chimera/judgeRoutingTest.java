package com.chimera;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.chimera.commerce.BudgetExceededException;
import com.chimera.judge.CfoJudge;
import com.chimera.judge.DefaultJudge;
import com.chimera.model.AgentResult;
import com.chimera.model.DailySpendSnapshot;
import com.chimera.model.GlobalStateSnapshot;
import com.chimera.model.JudgeRouting;
import com.chimera.model.ReviewDecision;
import com.chimera.model.TransactionRequest;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Contract tests for Judge confidence routing, OCC, and CFO exception behaviour.
 *
 * <p><b>Specs:</b> {@code specs/technical.md} §1 ({@code ReviewDecision}, {@code AgentResult}),
 * §2 task status / HITL fields; {@code research/architecture_strategy.md} §4 (HITL flow).
 * <b>User stories:</b> US-010 (thresholds), US-011 (sensitive topics), US-012 (OCC),
 * US-014 ({@link BudgetExceededException}).</p>
 *
 * <p>Tests that call {@link DefaultJudge#evaluate(AgentResult, GlobalStateSnapshot)} fail until routing is
 * implemented — they encode the NFR 1.1 table literally.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Judge routing and OCC contract")
class judgeRoutingTest {

    private static final UUID TASK = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private static final UUID WORKER = UUID.fromString("660e8400-e29b-41d4-a716-446655440001");
    private static final UUID AGENT = UUID.fromString("770e8400-e29b-41d4-a716-446655440002");

    @Mock
    private CfoJudge cfoJudge;

    static Stream<Arguments> confidenceRoutingCases() {
        return Stream.of(
                Arguments.of(0.91f, JudgeRouting.AUTO_EXECUTE, "NFR 1.1: >0.90 → auto-execute"),
                Arguments.of(0.85f, JudgeRouting.HITL_QUEUE, "NFR 1.1: 0.70–0.90 → HITL"),
                Arguments.of(0.70f, JudgeRouting.HITL_QUEUE, "NFR 1.1: lower inclusive band edge → HITL"),
                Arguments.of(0.69f, JudgeRouting.REJECT_RETRY, "NFR 1.1: <0.70 → reject + retry"));
    }

    @ParameterizedTest(name = "{2}")
    @MethodSource("confidenceRoutingCases")
    @DisplayName("DefaultJudge.evaluate applies US-010 confidence bands when versions align")
    void defaultJudgeAppliesConfidenceBands(float confidence, JudgeRouting expectedRouting, String specRef) {
        AgentResult result = new AgentResult(TASK, WORKER, true, confidence, "{}", 7);
        GlobalStateSnapshot state = new GlobalStateSnapshot(AGENT, 7);
        ReviewDecision decision = DefaultJudge.create().evaluate(result, state);
        assertEquals(expectedRouting, decision.routing(), specRef);
        assertTrue(decision.occCommitAllowed(), "OCC aligned — commit allowed pending US-011 overrides");
    }

    @Nested
    @DisplayName("Optimistic concurrency (US-012 / FR 6.1)")
    class OptimisticConcurrency {

        @Test
        @DisplayName("When Worker stateVersion != GlobalState currentStateVersion, occCommitAllowed is false")
        void stateDriftBlocksCommit() {
            AgentResult result = new AgentResult(TASK, WORKER, true, 0.95f, "{}", 3);
            GlobalStateSnapshot state = new GlobalStateSnapshot(AGENT, 7);
            ReviewDecision decision = DefaultJudge.create().evaluate(result, state);
            assertFalse(decision.occCommitAllowed(), "US-012: stale Worker result must not commit");
            assertEquals(JudgeRouting.REJECT_RETRY, decision.routing());
        }
    }

    @Nested
    @DisplayName("CFO path (US-014)")
    class CfoExceptions {

        @Test
        @DisplayName("CfoJudge.validate may throw BudgetExceededException when daily cap exceeded")
        void budgetExceededExceptionIsPartOfContract() {
            doThrow(new BudgetExceededException("US-014 daily cap"))
                    .when(cfoJudge)
                    .validate(any(TransactionRequest.class), any(DailySpendSnapshot.class));
            assertThrows(
                    BudgetExceededException.class,
                    () ->
                            cfoJudge.validate(
                                    new TransactionRequest(AGENT, "0xabc", 100f, "test", 1),
                                    new DailySpendSnapshot(AGENT, 49f, 50f)));
            verify(cfoJudge).validate(any(TransactionRequest.class), any(DailySpendSnapshot.class));
        }
    }
}
