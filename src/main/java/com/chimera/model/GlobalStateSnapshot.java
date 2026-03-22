package com.chimera.model;

import java.util.UUID;

/**
 * Authoritative OCC view for Judge comparison against {@link AgentResult#stateVersion()}.
 *
 * <p><b>SRS:</b> §6.1 OCC, Redis {@code agent:{id}:state}.
 * <b>User stories:</b> US-012 (ghost update prevention).</p>
 *
 * @param agentId              state owner
 * @param currentStateVersion  latest committed version
 */
public record GlobalStateSnapshot(
        UUID agentId,
        int currentStateVersion
) {
}
