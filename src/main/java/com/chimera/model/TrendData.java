package com.chimera.model;

import java.time.Instant;

/**
 * Normalized trend signal from MCP resources (perception path).
 *
 * <p>Opaque MCP metadata is stored as a JSON document string to avoid untyped maps in DTOs
 * (project Java rules; aligns with JSONB {@code raw_metadata} at persistence layer).</p>
 *
 * <p><b>SRS:</b> FR 2.0, §6.2 trend shape.
 * <b>User stories:</b> US-001 (poll resources), US-003 (cluster inputs).</p>
 *
 * @param platform         source label (e.g. {@code twitter})
 * @param topic            normalized topic key
 * @param viralityScore    ranking signal 0.0–1.0
 * @param fetchedAt        ingestion time
 * @param rawMetadataJson  JSON blob from MCP (may be empty object)
 */
public record TrendData(
        String platform,
        String topic,
        float viralityScore,
        Instant fetchedAt,
        String rawMetadataJson
) {
}
