package com.chimera.streams;

import com.chimera.model.AgentResult;

/**
 * Abstraction for appending Worker results to the Redis {@code review_queue} stream.
 *
 * <p><b>SRS:</b> §6.2 {@code AgentResult}, Redis §4.
 * <b>User stories:</b> US-009 (confidence on every Worker output), US-010 (Judge consumes results).</p>
 */
public interface ReviewQueuePublisher {

    /**
     * Serializes and appends a Worker result for Judge evaluation.
     *
     * @param result immutable result DTO including {@code confidenceScore} and {@code stateVersion}
     */
    void publish(AgentResult result);
}
