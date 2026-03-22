package com.chimera;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.chimera.commerce.BudgetExceededException;
import com.chimera.judge.DefaultCfoJudge;
import com.chimera.model.ApprovalStatus;
import com.chimera.model.BudgetCategory;
import com.chimera.model.TransactionRequest;
import com.chimera.model.TransactionRequestType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Contract: CFO Sub-Judge must reject over-budget governed transactions with {@link BudgetExceededException}.
 *
 * <p><b>Spec:</b> {@code specs/technical.md} §7.3 BR-2 (daily spend cap), §7.6 V-6 (over-budget → reject /
 * {@code BudgetExceededException} semantics), §5 NFR table (default $50 USDC/day per agent).
 * <b>User stories:</b> US-014. <b>Functional:</b> {@code specs/functional.md} commerce stories.</p>
 *
 * <p>Fails under TDD until {@link DefaultCfoJudge} (or another production {@link com.chimera.judge.CfoJudge})
 * implements real projection against remaining daily headroom.</p>
 */
@DisplayName("Governed budget validation (CFO / TransactionRequest)")
class BudgetValidationTest {

    private static final UUID AGENT =
            UUID.fromString("770e8400-e29b-41d4-a716-446655440002");
    private static final UUID TXN =
            UUID.fromString("990e8400-e29b-41d4-a716-446655440088");
    private static final UUID CORRELATION =
            UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    @Test
    @DisplayName("Over-budget TransactionRequest causes BudgetExceededException (§7.6 V-6, US-014)")
    void overBudgetRequestThrowsBudgetExceededException() {
        // Narrative: agent is already near default $50 USDC/day cap; this MCP-billed media line item exhausts headroom.
        TransactionRequest overBudget =
                new TransactionRequest(
                        TXN,
                        AGENT,
                        TransactionRequestType.MCP_BILLED_USAGE,
                        new BigDecimal("52.50"),
                        "USDC",
                        BudgetCategory.MEDIA_GENERATION,
                        "Luma Ray 2 clip render + upscale bundle — billed via governed MCP path",
                        4,
                        Instant.parse("2026-03-22T18:45:00Z"),
                        ApprovalStatus.PENDING,
                        null,
                        CORRELATION);

        assertThrows(
                BudgetExceededException.class,
                () -> DefaultCfoJudge.create().review(overBudget),
                "CFO Judge must throw BudgetExceededException when §7.6 V-6 / US-014 daily cap would be exceeded");
    }
}
