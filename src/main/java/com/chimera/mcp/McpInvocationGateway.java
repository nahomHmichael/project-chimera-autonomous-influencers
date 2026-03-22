package com.chimera.mcp;

/**
 * Boundary type for outbound tool and resource calls that must be satisfied exclusively via MCP hosts.
 *
 * <p>No HTTP SDK clients or vendor-specific adapters belong in {@code com.chimera} production packages;
 * future implementations delegate to an MCP Java host behind this interface.</p>
 *
 * <p><b>SRS:</b> §3.2 MCP integration, FR 4.0 (publish via MCP tools).
 * <b>User stories:</b> US-001 (MCP resources), US-007 ({@code twitter.post_tweet} / {@code instagram.publish_media}).</p>
 */
public interface McpInvocationGateway {
    // Scaffold — concrete MCP client wiring is out of scope for this module phase.
}
