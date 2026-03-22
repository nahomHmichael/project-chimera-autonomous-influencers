package com.chimera.model;

import java.time.Instant;
import java.util.Map;

/** Trend data from MCP perception resources — SRS FR 2.0 / US-001 */
public record TrendData(
    String platform,
    String topic,
    float viralityScore,
    Instant fetchedAt,
    Map<String, Object> rawMetadata
) {}
