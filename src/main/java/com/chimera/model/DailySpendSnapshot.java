package com.chimera.model;

import java.util.UUID;

/**
 * CFO-facing projection of spend against daily cap (e.g. Redis {@code agent:{id}:daily_spend}).
 *
 * <p><b>SRS:</b> Redis key schema §4, FR 5.2.
 * <b>User stories:</b> US-014 (max daily spend), US-013 (pre-flight affordability).</p>
 *
 * @param agentId        agent key
 * @param spentUsdcToday accumulated spend for current UTC window
 * @param dailyCapUsdc   configured cap (default $50 USDC in SRS narrative)
 */
public record DailySpendSnapshot(
        UUID agentId,
        float spentUsdcToday,
        float dailyCapUsdc
) {
}
