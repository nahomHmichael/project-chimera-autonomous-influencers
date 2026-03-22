package com.chimera.model;

/**
 * Maps spend to CFO policy lines (caps, alerts, blocked categories).
 *
 * <p><b>Spec:</b> {@code specs/technical.md} §7.4 ({@code budget_category}).
 * <b>User stories:</b> US-014 (daily cap by category deployment config).</p>
 */
public enum BudgetCategory {

    MEDIA_GENERATION,

    PUBLISHING,

    INFRA_MCP,

    TRENDS,

    OTHER
}
