package com.chimera.orchestration;

/**
 * Facade for wiring Planner → {@code task_queue} → Worker → {@code review_queue} → Judge loops.
 *
 * <p><b>SRS:</b> §3.1 FastRender hierarchical swarm.
 * <b>User stories:</b> US-016 (campaign DAG), US-010 (Judge routing).</p>
 */
public interface SwarmOrchestrationService {

    /**
     * Reserved hook for a single orchestration tick (scaffold — no behaviour yet).
     */
    void tick();
}
