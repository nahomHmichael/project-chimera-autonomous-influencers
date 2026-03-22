package com.chimera.model;

import java.util.List;

/**
 * Immutable context embedded in {@link AgentTask}.
 *
 * <p><b>SRS:</b> §6.2 Schema 1 {@code TaskContext}.
 * <b>User stories:</b> US-004 (persona constraints), US-016 (goal description).</p>
 *
 * @param goalDescription    natural-language campaign sub-goal
 * @param personaConstraints voice / safety lines from SOUL.md
 * @param requiredResources  MCP resource URIs (e.g. {@code mcp://twitter/mentions/123})
 */
public record TaskContext(
        String goalDescription,
        List<String> personaConstraints,
        List<String> requiredResources
) {
}
