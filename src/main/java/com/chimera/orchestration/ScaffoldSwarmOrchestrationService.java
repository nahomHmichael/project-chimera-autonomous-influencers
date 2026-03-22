package com.chimera.orchestration;

import org.springframework.stereotype.Service;

/**
 * No-op implementation until Redis stream consumers and MCP bridges exist.
 *
 * <p><b>SRS:</b> §3.1 (orchestration host placeholder).
 * <b>User stories:</b> US-018 (scaffold compiles under CI).</p>
 */
@Service
public class ScaffoldSwarmOrchestrationService implements SwarmOrchestrationService {

    /**
     * {@inheritDoc}
     */
    @Override
    public void tick() {
        // Intentionally empty — connect streams, Workers, and Judge in a future milestone.
    }
}
