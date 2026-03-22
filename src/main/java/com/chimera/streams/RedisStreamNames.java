package com.chimera.streams;

/**
 * Redis Stream key names for Planner → Worker → Judge flow.
 *
 * <p>Aligned with {@code specs/technical.md} §4 Redis key schema.</p>
 *
 * <p><b>SRS:</b> §3.1 task distribution, §6.2 queue semantics.
 * <b>User stories:</b> US-016 (Planner DAG tasks), US-009 (results to Judge).</p>
 */
public final class RedisStreamNames {

    /** Planner publishes {@link com.chimera.model.AgentTask} envelopes to this stream. */
    public static final String TASK_QUEUE = "task_queue";

    /** Workers publish {@link com.chimera.model.AgentResult} envelopes to this stream. */
    public static final String REVIEW_QUEUE = "review_queue";

    private RedisStreamNames() {
    }
}
