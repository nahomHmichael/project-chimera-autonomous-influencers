package com.chimera.worker.trend;

import com.chimera.model.AgentResult;
import com.chimera.model.AgentTask;
import com.chimera.worker.Worker;

/**
 * Worker role: polls MCP resources for trends and emits clustered Trend Alerts.
 *
 * <p><b>SRS:</b> §3.1 Worker roles, §6.2 {@code AgentResult} / {@code TrendData}.
 * <b>User stories:</b> US-001 (4h MCP poll), US-003 (clustering), US-009 ({@code confidenceScore}).</p>
 *
 * <p>Scaffold: {@link #execute(AgentTask)} throws until MCP orchestration is implemented.</p>
 */
public final class TrendFetcherWorker implements Worker {

    /**
     * {@inheritDoc}
     */
    @Override
    public String workerRole() {
        return "Trend Fetcher Worker";
    }

    /**
     * {@inheritDoc}
     *
     * @throws UnsupportedOperationException until US-001/US-003 implementation exists
     */
    @Override
    public AgentResult execute(AgentTask task) {
        throw new UnsupportedOperationException(
                "Implement MCP resource polling and Trend Alerts per US-001, US-003; "
                        + "return AgentResult with confidenceScore in [0,1] and JSON payload (specs/technical.md §1)");
    }

    /**
     * Scaffold factory for tests and future DI.
     *
     * @return new instance
     */
    public static TrendFetcherWorker create() {
        return new TrendFetcherWorker();
    }
}
